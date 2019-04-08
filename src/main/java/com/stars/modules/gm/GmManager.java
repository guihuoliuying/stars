package com.stars.modules.gm;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhaowenshuo on 2016/1/15.
 */
public class GmManager {
    private static Map<String, GmHandler> handlers = new HashMap<>();

    public static ConcurrentHashMap<Long,Integer> GM_MESSAGE_REDPOINTS = new ConcurrentHashMap<>();

    public static void reg(String command, GmHandler handler) {
        command = command.toLowerCase();
        if (handlers.containsKey(command)) {
            throw new IllegalArgumentException("GM命令已存在");
        }
        handlers.put(command, handler);
    }

    public static GmHandler get(String command) {
        return handlers.get(command);
    }

    public static boolean contains(String command) {
        return handlers.containsKey(command);
    }

}
