package com.xiangqian.ws.example.chat.service;

import com.xiangqian.ws.annotation.*;
import com.xiangqian.ws.scope.WSRequest;
import com.xiangqian.ws.scope.WSSession;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiangqian
 * @date 14:50 2020/01/04
 */
@Slf4j
// @WSService：指定当前Class为一个回话服务
@WSService(path = "/user") // path：指定回话服务访问路径
public class UserService {

    private WSSession session;

    @OnInit
    private void init(WSRequest request) {
        log.info("init");
        log.info("request param: " + request.getParameters());
    }

    /**
     * 连接成功时回调方法
     *
     * @param session
     */
    @OnOpen
    private void open(WSSession session) {
        log.info("open");
        this.session = session;

        // add
        USER_LIST.add(this);
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

        log.info("message: " + text);

        // 群发
        for (UserService user : USER_LIST) {
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
     *
     * @param throwable
     */
    @OnError
    private void error(Throwable throwable) {
        log.info("error");
    }

    //
    private static class UserServiceManager {
        static Map<String, UserService> userServiceMap;

        static {
            userServiceMap = new ConcurrentHashMap<>();
        }

        static void add(String username, UserService userService) {
            userServiceMap.put(username, userService);
        }

        static void remove(String username) {
            userServiceMap.remove(username);
        }

        static void sendMessage(String username, String message) {
            UserService userService = userServiceMap.get(username);
            if (userService != null) {
                userService.session.send(message);
            }
        }

        static Set<String> userList() {
            return userServiceMap.keySet();
        }

    }


}
