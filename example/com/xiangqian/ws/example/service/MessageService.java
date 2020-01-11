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
