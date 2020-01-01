package com.xiangqian.ws.exception;

/**
 * 未定义异常
 *
 * @author xiangqian
 * @date 16:34 2020/01/01
 */
public class UndefinedException extends RuntimeException {

    public UndefinedException() {
        super();
    }

    public UndefinedException(String message) {
        super(message);
    }
}
