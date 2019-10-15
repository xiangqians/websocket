package com.xq.ws.util;

/**
 * IO工具类
 *
 * @author xiangqian
 */
public class IOUtils {

    /**
     * 释放IO资源
     *
     * @param autoCloseables
     */
    public static void quietlyClosed(AutoCloseable... autoCloseables) {
        if (autoCloseables == null) {
            return;
        }
        for (AutoCloseable autoCloseable : autoCloseables) {
            if (autoCloseable == null) {
                continue;
            }
            try {
                autoCloseable.close();
            } catch (Exception e) {
            }
        }
    }

}
