package com.xq.ws.exception;

/**
 * 未定义异常
 * @author xiangqian
 */
public class UndefinedException extends RuntimeException {
    public UndefinedException() {
        super();
    }

    public UndefinedException(String message) {
        super(message);
    }
}
