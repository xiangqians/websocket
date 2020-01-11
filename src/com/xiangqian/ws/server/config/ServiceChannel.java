package com.xiangqian.ws.server.config;

import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xiangqian
 * @date 14:00 2020/01/11
 */
@NoArgsConstructor
@Data
public class ServiceChannel {

    private WebSocketServerHandshaker webSocketServerHandshaker; // WS握手对象
    private Service service;
    private Object serviceInstance;

    public ServiceChannel(Service service) throws IllegalAccessException, InstantiationException {
        this.service = service;
        this.serviceInstance = this.service.getClazz().newInstance();
    }

}
