package com.xiangqian.ws.server.config;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

/**
 * @author xiangqian
 * @date 13:59 2020/01/11
 */
@NoArgsConstructor
@Data
public class Service {

    /**
     * server服务路径{@link com.xiangqian.ws.annotation.WSService}
     */
    private String path;

    /**
     * service class
     */
    private Class<?> clazz;

    /**
     * server {@link com.xiangqian.ws.annotation.OnInit}
     */
    private Method onInit;

    /**
     * server {@link com.xiangqian.ws.annotation.OnOpen}
     */
    private Method onOpen;

    /**
     * server {@link com.xiangqian.ws.annotation.OnMessage}
     */
    private Method onMessage;

    /**
     * server {@link com.xiangqian.ws.annotation.OnClose}
     */
    private Method onClose;

    /**
     * server {@link com.xiangqian.ws.annotation.OnError}
     */
    private Method onError;

}
