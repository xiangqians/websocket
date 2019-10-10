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
