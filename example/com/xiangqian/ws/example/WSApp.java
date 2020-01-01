package com.xiangqian.ws.example;

import com.xiangqian.ws.annotation.WSServerConfiguration;
import com.xiangqian.ws.server.WSServerApplication;

/**
 * @author xiangqian
 * @date 16:48 2020/01/01
 */
@WSServerConfiguration(port = 8080, basePackages = {"com.xiangqian.ws.example.service"}, path = "/ws")
public class WSApp {

    public static void main(String[] args) {
        WSServerApplication.run(WSApp.class, args);
    }

}
