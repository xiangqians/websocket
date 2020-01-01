package com.xiangqian.ws.util;

/**
 * @author xiangqian
 * @date 15:40 2020/01/01
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
