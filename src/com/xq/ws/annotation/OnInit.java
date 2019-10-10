package com.xq.ws.annotation;

import io.netty.handler.codec.http.FullHttpRequest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 连接初始化时
 *
 * @author xiangqian
 * <p>
 * param
 * @see FullHttpRequest
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnInit {
}
