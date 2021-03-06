# WebSocket

## 概述
基于Netty框架实现WebSocket


## 快速使用
（见example包）

#### 创建回话服务Class
```java
package com.xiangqian.ws.example.service;

import com.xiangqian.ws.annotation.*;
import com.xiangqian.ws.scope.WSRequest;
import com.xiangqian.ws.scope.WSSession;
import com.xiangqian.ws.util.StringUtils;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author xiangqian
 * @date 11:09 2020/01/05
 */
@Slf4j

// @WSService：指定当前Class为一个回话服务
@WSService(path = "/message") // path：指定回话服务访问路径
public class MessageService {

    private WSSession session;

    /**
     * 连接初始化时
     *
     * @param request
     */
    @OnInit
    private boolean init(WSRequest request) {
        log.debug("init, request param: " + request.getParameters());
        return true;
    }


    /**
     * 连接成功时回调方法
     *
     * @param session
     */
    @OnOpen
    private void open(WSSession session) {
        log.debug("open");
        this.session = session;
    }


    /**
     * 接收到消息时回调方法
     *
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

        log.debug("message: " + text);
        session.send(text);
    }

    /**
     * 连接关闭时回调方法
     */
    @OnClose
    private void close() {
        log.debug("close");
    }

    /**
     * 连接发生错误时回调方法
     *
     * @param throwable
     */
    @OnError
    private void error(Throwable throwable) {
        log.error("error", throwable);
    }

}
```


### 创建WebSocket启动类
```java
package com.xiangqian.ws.example;

import com.xiangqian.ws.annotation.WSServerConfiguration;
import com.xiangqian.ws.server.WSServerApplication;

/**
 * @author xiangqian
 * @date 11:14 2020/01/05
 */
// @WSServerConfiguration：指定WebSocket应用配置
@WSServerConfiguration(port = 8080, // 指定WebSocket监听端口
        path = "/ws", // 指定WebSocket访问根路径
        basePackages = {"com.xiangqian.ws.example.service"} // 指定WebSocket回话服务Class所在的包
)
public class WSApplication {

    public static void main(String[] args) {
        // 运行WebSocket应用
        WSServerApplication.run(WSApplication.class, args);
    }

}
```

### 启动WebSocket应用

#### 默认使用@WSServerConfiguration配置
java -jar xxx.jar

#### 动态修改WebSocket配置
java -jar xxx.jar --ws.server.port=8089 --ws.server.path=/ws2


### HTML发起WebSocket连接
```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>WebSocket</title>
</head>

<script type="text/javascript">

    function WS() {
    }

    WS.webSocket = null;

    // connection
    WS.connection = function () {
        if (WS.webSocket != null) {
            WS.addInfo("WebSocket connected");
            return;
        }

        if (!window.WebSocket) {
            window.WebSocket = window.MozWebSocket;
        }

        // 判断当前浏览器是否支持WebSocket
        // var flag  = 'WebSocket' in window;
        var flag = window.WebSocket;
        if (!flag) {
            WS.addInfo("Your browser does not support Web Socket.");
            return;
        }


        var url = document.getElementById("url").value;
        WS.webSocket = new WebSocket(url);
        WS.clearInfo();

        // open
        // 连接成功建立的回调方法
        WS.webSocket.onopen = function (event) {
            WS.addInfo("Web Socket opened!");
            WS.clearMessage();
        };

        // message
        // 接收到消息的回调方法
        WS.webSocket.onmessage = function (event) {
            WS.addInfo("Web Socket received message!");
            WS.addMessage(event.data);
        };

        // close
        // 连接关闭的回调方法
        WS.webSocket.onclose = function () {
            WS.addInfo("Web Socket closed!");
            WS.webSocket = null;
        };

        // error
        // 连接发生错误的回调方法
        WS.webSocket.onerror = function (event) {
            WS.addInfo("Web Socket connection error!");
            WS.webSocket = null;
        };
    };

    WS.send = function () {
        if (WS.webSocket == null) {
            WS.addInfo("No connection established");
            return;
        }

        var data = document.getElementById("data").value;
        WS.webSocket.send(data);
        // document.getElementById("data").value = "";
    };

    WS.close = function () {
        if (WS.webSocket != null) {
            WS.webSocket.close();
        }
    };

    WS.clearInfo = function () {
        var infoEle = document.getElementById("info");
        infoEle.innerHTML = "";
    };

    WS.clearMessage = function () {
        var messageEle = document.getElementById("message");
        messageEle.innerHTML = "";
    };

    WS.addInfo = function (data) {
        WS.addData("info", data);
    };

    WS.addMessage = function (data) {
        WS.addData("message", data);
    };

    WS.addData = function (eleId, data) {
        //
        var dataDivEle = document.createElement('div');

        // tag
        var tagSpanEle = document.createElement('span');
        tagSpanEle.style = "color:blue;";
        tagSpanEle.innerHTML = "[" + WS.curDate() + "]" + ":&nbsp;";
        dataDivEle.appendChild(tagSpanEle);

        // data
        var dataSpanEle = document.createElement('span');
        dataSpanEle.style = "color: black;";
        dataSpanEle.innerHTML = data;
        dataDivEle.appendChild(dataSpanEle);

        // ele
        var ele = document.getElementById(eleId);
        ele.appendChild(dataDivEle);
        ele.scrollTop = ele.scrollHeight;
    };

    WS.curDate = function () {
        var date = new Date();
        var arr = new Array();

        // year
        arr.push(date.getFullYear());
        arr.push("/");

        // month
        var month = date.getMonth() + 1;
        arr.push(month < 10 ? ("0" + month) : month);
        arr.push("/");

        // date
        var d = date.getDate();
        arr.push(d < 10 ? ("0" + d) : d);
        arr.push(" ");

        // hours
        arr.push(date.getHours());
        arr.push(":");

        // minutes
        arr.push(date.getMinutes());
        arr.push(":");

        // seconds
        arr.push(date.getSeconds());
        arr.push(".");

        // milliseconds
        arr.push(date.getMilliseconds());

        return arr.join("");
    };

    // 监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
    window.onbeforeunload = function () {
        WS.webSocket.close();
    }

    window.onload = function () {
        document.getElementById("connection").onclick = WS.connection;
        document.getElementById("send").onclick = WS.send;
        document.getElementById("close").onclick = WS.close;
        document.getElementById("clearInfo").onclick = WS.clearInfo;
        document.getElementById("clearMessage").onclick = WS.clearMessage;
        document.onkeydown = function (event) {
            var keycode = event.keyCode;
            if (keycode == 13 && event.srcElement.id === "data") {
                WS.send();
            }
        }
    }

</script>

<body>

<h2>WebSocket</h2>

<br/>
<hr/>
<br/>

<table align="center">
    <tr>
        <td>服务器地址：</td>
        <td>
            <input id="url" type="text" value="ws://127.0.0.1:8080/ws/message" style="width:260px;">
            <button id="connection">connection</button>
            <button id="close">close</button>
        </td>
    </tr>

    <tr>
        <td>info：</td>
        <td>
            <button id="clearInfo">clear</button>
        </td>
    </tr>
    <tr>
        <td></td>
        <td>
            <div id="info" style="height:160px;width:800px;overflow:auto;background:#EEEEEE;"></div>
        </td>
    </tr>

    <tr>
        <td colspan="2">
            <br/>
            <hr/>
            <br/>
        </td>
    </tr>

    <tr>
        <td>input：</td>
        <td>
            <input id="data" type="text" value="" style="width:260px;">
            <button id="send">send</button>
        </td>
    </tr>

    <tr>
        <td colspan="2">
            <br/>
            <hr/>
            <br/>
        </td>
    </tr>

    <tr>
        <td>message：&nbsp;&nbsp;&nbsp;&nbsp;</td>
        <td>
            <button id="clearMessage">clear</button>
        </td>
    </tr>

    <tr>
        <td></td>
        <td>
            <div id="message" style="height:300px;width:800px;overflow:auto;background:#EEEEEE;"></div>
        </td>
    </tr>

</table>

</body>
</html>
```

