package com.stars.core.hotupdate;

import com.yinhan.YinHanClassLoader;
import com.stars.util.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wuyuxing on 2017/1/5.
 */
public class YinHanHotUpdateManager {

    public static ConcurrentHashMap<String, Class> hotUpdateClass = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Object> hotUpdateCache = new ConcurrentHashMap<>(); //热更临时数据缓存，热更时配用

    public static void init(){
        hotUpdateClass.put("CommManager",CommManagerProxy.class);
    }

    public static boolean hotUpdateClass(String className) {
        try {
            Class c = hotUpdateClass.get(className);
            if (c != null) {
                Object o = c.newInstance();
                Method m = c.getMethod("reload");
                m.invoke(o);
                return true;
            }
            LogUtil.info("没找到类 className =" + className);
            return false;
        } catch (Exception e) {
            LogUtil.error("hotUpdateClass", e.getMessage(), e);
        }
        return false;
    }

    public static Class loadClass(String name) throws ClassNotFoundException, IOException {
        YinHanClassLoader loader = new YinHanClassLoader();
        String path = "./comm/" + name + ".class";
        byte data[] = getBytes(path);
        if (data != null && data.length > 0) {
            Class ncl = loader.findClass(data);
            return ncl;
        }
        return null;
    }

    private static byte[] getBytes(String name) {
        File file = new File(name);
        long len = file.length();
        byte raw[] = new byte[(int) len];
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            int r = fin.read(raw);
            if (r != len) {
                return null;
            }
            return raw;
        } catch (Exception e) {
            LogUtil.error("getBytes", e.getMessage(), e);
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (Exception e) {
                LogUtil.error("getBytes1", e.getMessage(), e);
            }
        }
        return null;
    }

    public static ConcurrentHashMap<String, Object> getHotUpdateCache() {
        return hotUpdateCache;
    }

    public static void setHotUpdateCache(ConcurrentHashMap<String, Object> hotUpdateCache) {
        YinHanHotUpdateManager.hotUpdateCache = hotUpdateCache;
    }

    public static boolean needHotUpdate(String key) {
        if (!hotUpdateCache.containsKey(key)) {
            return hotUpdateCache.putIfAbsent(key, true) == null;
        }
        return false;
    }

    public static <T> T globalCache(String key) {
        return (T) hotUpdateCache.get(key);
    }

    public static void globalCache(String key, Object val) {
        hotUpdateCache.put(key, val);
    }
}
