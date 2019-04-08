package com.stars.services.summary;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhaowenshuo on 2016/9/14.
 */
public class DummySummaryComponentInvocationHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class returnClass = method.getReturnType();
        if ((returnClass == Boolean.class || returnClass == boolean.class)
                && method.getName().equals("isDummy")) {
            return true;
        }
        if (returnClass == String.class) {
            return "";
        }
        if (returnClass == long.class || returnClass == Long.class) {
            return (long) 0;
        }
        if (returnClass == int.class || returnClass == Integer.class) {
            return (int) 0;
        }
        if (returnClass == short.class || returnClass == Short.class) {
            return (short) 0;
        }
        if (returnClass == byte.class || returnClass == Byte.class) {
            return (byte) 0;
        }
        if (returnClass == boolean.class || returnClass == Boolean.class) {
            return false;
        }
        if (returnClass == float.class || returnClass == Float.class) {
            return 0.0F;
        }
        if (returnClass == double.class || returnClass == Double.class) {
            return 0.0D;
        }
        if (returnClass == char.class || returnClass == Character.class) {
            return (char) 0;
        }
        if (returnClass == Map.class) {
            return new HashMap<>();
        }
        if (returnClass == ConcurrentMap.class) {
            return new ConcurrentHashMap<>();
        }
        if (returnClass == List.class) {
            return new ArrayList<>();
        }
        if (returnClass == Set.class) {
            return new HashSet<>();
        }
        Constructor constructor = returnClass.getConstructor();
        if (constructor != null) {
            return constructor.newInstance();
        }
        return null;
    }

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
    }

}