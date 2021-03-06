package com.xiangqian.ws.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 连接发生错误时回调方法
 *
 * <p>
 * 方法形参：
 * param 1 {@link Throwable}
 * <p>
 *
 * @author xiangqian
 * @date 16:29 2020/01/01
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnError {
}
