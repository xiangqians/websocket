# WebSocket

## 概述
基于Netty框架实现WebSocket

## 快速使用

#### 创建回话服务Class
```java
package com.xq.ws.example.service;

import com.xq.ws.Session;
import com.xq.ws.annotation.*;
import com.xq.ws.http.WSHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xiangqian
 */
@Slf4j

// @WebSocketServer: 指定当前Class为一个回话服务
// path: 指定回话服务访问路径
@WebSocketServer(path = "/user")
public class UserService {

    private static final List<UserService> USER_LIST = new ArrayList<>();

    private Session session;

    @OnInit
    private void init(WSHttpRequest request){
        log.info("init");
        log.info("request param: " + request.getParameters());
    }

    /**
     * 连接成功时回调方法
     */
    @OnOpen
    private void open(Session session) {
        log.info("open");
        this.session = session;

        // add
        USER_LIST.add(this);
    }

    /**
     * 接收到消息时回调方法
     * @param webSocketFrame
     * @throws IOException
     */
    @OnMessage
    private void message(WebSocketFrame webSocketFrame) throws IOException {
        String text = null;

        // 文本消息
        if (webSocketFrame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) webSocketFrame;
            text = textWebSocketFrame.text();
        }
        // 二进制消息
        else if (webSocketFrame instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame) webSocketFrame;
            text = binaryWebSocketFrame.content().toString();
        }
        //
        else {
            text = webSocketFrame.toString();
        }

        log.info("message: " + text);

        // 群发
        for(UserService user : USER_LIST){
            user.session.send(text);
        }
    }

    /**
     * 连接关闭时回调方法
     */
    @OnClose
    private void close() {
        log.info("close");
    }

    /**
     * 连接发生错误时回调方法
     * @param throwable
     */
    @OnError
    private void error(Throwable throwable) {
        log.info("error");
    }

}
```

### 创建WebSocket启动类
```java
package com.xq.ws.example;

import com.xq.ws.annotation.WebSocketApplication;
import com.xq.ws.server.WebSocketApp;

/**
 * @author xiangqian
 */
// @WebSocketApplication: 指定当前Class为WebSocket应用
// port: 指定WebSocket监听端口
// path: 指定WebSocket访问路径
// basePackages: 指定回话服务Class所在的包
@WebSocketApplication(port = 8080, path = "/websocket", basePackages = {"com.xq.ws.example.service"})
public class MainApplication {
    public static void main(String[] args) {
        try {
            // 运行WebSocket应用
            WebSocketApp.run(MainApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Html
```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Index</title>
</head>

<script type="text/javascript">

    function Chat() {
    }

    Chat.webSocket = null;
    Chat.connection = function () {
        if (Chat.webSocket != null) {
            Chat.setStatusInfo("WebSocket connected");
            return;
        }

        // 判断当前浏览器是否支持WebSocket
        if (!('WebSocket' in window)) {
            Chat.setStatusInfo("当前浏览器 Not support WebSocket!");
            return;
        }

        var url = document.getElementById("url").value;
        Chat.webSocket = new WebSocket(url);

        // 连接成功建立的回调方法
        Chat.webSocket.onopen = function (event) {
            Chat.setStatusInfo("WebSocket connection success!");
            Chat.clear();
        };

        // 接收到消息的回调方法
        Chat.webSocket.onmessage = function (event) {
            Chat.setStatusInfo("on message");
            Chat.setMessage(event.data);
        };
        // 连接关闭的回调方法
        Chat.webSocket.onclose = function () {
            Chat.setStatusInfo("WebSocket connection close!");
            Chat.webSocket = null;
        };

        // 连接发生错误的回调方法
        Chat.webSocket.onerror = function (event) {
            Chat.setStatusInfo("WebSocket connection error!");
        };
    };
    Chat.clear = function () {
        var messageElement = document.getElementById("message");
        messageElement.innerHTML = "";
    };
    Chat.setStatusInfo = function (statusInfo) {
        document.getElementById('statusInfo').innerHTML = statusInfo;
    };
    Chat.setMessage = function (message) {
        var dataObj = JSON.parse(message);

        //
        var divEle = document.createElement('div');

        //
        var tagSpanEle = document.createElement('span');
        tagSpanEle.style = "color:blue;";
        var tag = dataObj.tag;
        tag += "[";
        var date = new Date();
        tag += date.getFullYear() + "-";
        tag += (date.getMonth() + 1) + "-";
        tag += date.getDate() + " ";
        tag += date.getHours() + ":";
        tag += date.getMinutes() + ":";
        tag += date.getSeconds() + ".";
        tag += date.getMilliseconds();
        tag += "]";
        tag += ":&nbsp;";
        tagSpanEle.innerHTML = tag;
        divEle.appendChild(tagSpanEle);

        //
        var dataSpanEle = document.createElement('span');
        dataSpanEle.style = "color: black;";
        dataSpanEle.innerHTML = dataObj.data;
        divEle.appendChild(dataSpanEle);

        //
        document.getElementById('message').appendChild(divEle);
    };
    Chat.send = function () {
        if (Chat.webSocket == null) {
            Chat.setStatusInfo("No connection established");
            return;
        }

        var dataObj = {};
        dataObj.tag = document.getElementById("tag").value;
        dataObj.data = document.getElementById("data").value;
        var dataJson = JSON.stringify(dataObj);
        Chat.webSocket.send(dataJson);
        document.getElementById("data").value = "";
    };

    // 监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
    window.onbeforeunload = function () {
        Chat.webSocket.close;
    }

    window.onload = function () {
        document.getElementById("connection").onclick = Chat.connection;
        document.getElementById("send").onclick = Chat.send;
        document.getElementById("clear").onclick = Chat.clear;
        document.onkeydown = function (event) {
            var keycode = event.keyCode;
            if (keycode == 13 && event.srcElement.id === "data") {
                Chat.send();
            }
        }
    }
</script>

<body>

<br/>
<input id="url" type="text" value="ws://127.0.0.1:8080/websocket/user" style="width:260px;">
<button id="connection">connection</button>
<br/><br/>
<hr/>
<br/>
<table>
    <tr>
        <td>tag</td>
        <td><input id="tag" type="text" value="Stephen Chow" style="width:160px;"></td>
    </tr>
    <tr>
        <td>data</td>
        <td><input id="data" type="text" value="" style="width:260px;"></td>
        <td>
            <button id="send">send</button>
        </td>
    </tr>
</table>

<br/>
<hr/>
<br/>
status info:
<div id="statusInfo" style="display:inline;">$status info</div>

<br/>
<hr/>
Message: &nbsp;&nbsp;&nbsp;&nbsp;<button id="clear">clear</button> &nbsp;
<br/>
<br/>
<div id="message" style="height:300px;width:800px;overflow:auto;background:#EEEEEE;"></div>
</body>
</html>
```