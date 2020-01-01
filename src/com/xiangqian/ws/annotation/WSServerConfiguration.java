package com.xiangqian.ws.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识WebSocket应用程序
 *
 * @author xiangqian
 * @date 16:30 2020/01/01
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WSServerConfiguration {

    // WebSocket监听端口
    int port();

    // WebSocket请求根路径
    String path();

    // 扫描Server所在的包
    String[] basePackages();

}
