package com.stars.util;

import java.util.Collection;
import java.util.Map;

/**
 * Created by jx on 2015/1/13.
 */
public class EmptyUtil {
	
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        return false;
    }

    public static boolean isEmpty(String str) {
        return ((str == null) || (str.trim().length() == 0));
    }

    public static boolean isNotEmpty(String str) {
        return (!isEmpty(str));
    }

    public static boolean isEmpty(Collection collection) {
        return ((collection == null) || (collection.size() == 0));
    }

    public static boolean isNotEmpty(Collection collection) {
        return (!isEmpty(collection));
    }

    public static boolean isEmpty(Map map) {
        return ((map == null) || (map.size() == 0));
    }

    public static boolean isNotEmpty(Map map) {
        return (!isEmpty(map));
    }

    public static boolean isEmpty(Object[] array) {
        return (array == null || array.length == 0);
    }

    public static boolean isNotEmpty(Object[] array) {
        return (!isEmpty(array));
    }
    
}
