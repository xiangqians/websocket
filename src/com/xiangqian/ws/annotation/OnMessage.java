package com.xiangqian.ws.annotation;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接收到消息时回调方法
 *
 * <p>
 * 方法形参：
 * param 1 {@link WebSocketFrame}
 * <p>
 *
 * @author xiangqian
 * @date 16:29 2020/01/01
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnMessage {
}
