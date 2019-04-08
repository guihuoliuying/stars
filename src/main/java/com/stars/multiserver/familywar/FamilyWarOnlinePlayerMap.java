package com.stars.multiserver.familywar;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenkeyu on 2017-06-15.
 */
public class FamilyWarOnlinePlayerMap {
    private static ConcurrentHashMap<Long, Boolean> roleId2Online = new ConcurrentHashMap<>();

    public static void roleOnline(Set<Long> roleIds) {
        for (long roleId : roleIds) {
            roleOnline(roleId);
        }
    }

    public static void roleOffline(Set<Long> roleIds) {
        for (long roleId : roleIds) {
            roleOffline(roleId);
        }
    }

    public static void roleOnline(long roleId) {
        roleId2Online.put(roleId, true);
    }

    public static void roleOffline(long roleId) {
        roleId2Online.put(roleId, false);
    }

    public static boolean isRoleOnline(long roleId) {
        if (!roleId2Online.containsKey(roleId)) return false;
        return roleId2Online.get(roleId);
    }
}

