package com.xiangqian.ws.example.chat;

import com.xiangqian.ws.annotation.WSServerConfiguration;
import com.xiangqian.ws.server.WSServerApplication;

/**
 * @author xiangqian
 * @date 16:48 2020/01/01
 */
// @WSApplication：指定当前Class为WebSocket应用
@WSServerConfiguration(port = 8080, // 指定WebSocket监听端口
        path = "/char", // 指定WebSocket访问根路径
        basePackages = {"com.xiangqian.ws.example.chat"} // 指定WebSocket回话服务Class所在的包
)
public class ChatApplication {

    public static void main(String[] args) {
        // 运行WebSocket应用
        WSServerApplication.run(ChatApplication.class, args);
    }

}
