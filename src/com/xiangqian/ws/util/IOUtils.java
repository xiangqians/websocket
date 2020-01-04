package com.xiangqian.ws.util;

/**
 * @author xiangqian
 * @date 15:40 2020/01/01
 */
public class IOUtils {

    public static final int KB = 1024; // 1KB
    public static final int MB = 1024 * 1024; // 1MB

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
