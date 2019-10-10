package com.xq.ws.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 连接发生错误时回调方法
 *
 * @author xiangqian
 *
 * <p>
 * param
 * @see Throwable
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnError {
}
