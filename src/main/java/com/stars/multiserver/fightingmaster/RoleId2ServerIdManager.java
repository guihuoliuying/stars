package com.stars.multiserver.fightingmaster;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhouyaohui on 2016/11/16.
 */
public class RoleId2ServerIdManager {
    private static final ConcurrentMap<Long, Integer> map = new ConcurrentHashMap<>();

    public static void put(long roleId, int serverId) {
        map.put(roleId, serverId);
    }

    public static int get(long roleId) {
        return map.get(roleId);
    }

    public static void remove(long roleId) {
        map.remove(roleId);
    }
}
