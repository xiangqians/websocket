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