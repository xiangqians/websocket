package com.xq.ws.annotation;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接收到消息时回调方法
 *
 * @author xiangqian
 * <p>
 * param
 * @see WebSocketFrame
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnMessage {
}
