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