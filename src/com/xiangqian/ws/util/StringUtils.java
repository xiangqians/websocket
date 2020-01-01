package com.xiangqian.ws.util;

/**
 * @author xiangqian
 * @date 16:59 2020/01/01
 */
public class StringUtils {

    public static String trim(Object obj) {
        return obj == null ? "" : obj.toString().trim();
    }

}
