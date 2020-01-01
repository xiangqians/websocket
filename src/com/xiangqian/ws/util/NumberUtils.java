package com.xiangqian.ws.util;

/**
 * @author xiangqian
 * @date 17:00 2020/01/01
 */
public class NumberUtils {

    public static int convert2int(Object obj, int defaultValue) {
        try {
            return Integer.parseInt(StringUtils.trim(obj));
        } catch (Exception e) {
        }
        return defaultValue;
    }


}
