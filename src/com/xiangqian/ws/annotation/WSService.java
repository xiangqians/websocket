package com.xiangqian.ws.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识WebSocket Service
 *
 * @author xiangqian
 * @date 16:30 2020/01/01
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WSService {

    // service服务路径
    String path();

}
