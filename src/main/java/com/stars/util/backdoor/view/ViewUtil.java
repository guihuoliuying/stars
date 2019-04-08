package com.stars.util.backdoor.view;

/**
 * Created by zws on 2015/11/5.
 */
public class ViewUtil {

    public static String toStr(Object obj, String defaultValue) {
        return obj != null ? obj.toString() : defaultValue;
    }

    public static int toInt(String obj, int defaultValue) {
        return obj != null ? Integer.parseInt(obj) : defaultValue;
    }

    public static long toLong(String obj, long defaultValue) {
        return obj != null ? Long.parseLong(obj) : defaultValue;
    }

    public static short toShort(String obj, short defaultValue) {
        return obj != null ? Short.parseShort(obj) : defaultValue;
    }

}
