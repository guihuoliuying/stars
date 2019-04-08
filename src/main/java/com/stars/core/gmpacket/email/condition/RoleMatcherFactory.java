package com.stars.core.gmpacket.email.condition;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * 负责角色对于全服邮件的资格匹配器工厂
 * Created by huwenjun on 2017/3/27.
 */
public class RoleMatcherFactory {
    private static Map<Integer, Class<? extends RoleMatcher>> roleMatcherMap = new HashMap<>();
    public static final int PASS = 1;
    public static final int NO_PASS = 0;

    static {
        roleMatcherMap.put(RoleMatcher.LevelRoleMatcher, LevelRoleMatcher.class);
        roleMatcherMap.put(RoleMatcher.CreateTimeRoleMatcher, CreateTimeRoleMatcher.class);
        roleMatcherMap.put(RoleMatcher.ChannelRoleMatcher, ChannelMatcher.class);
    }

    public static RoleMatcher getInstance(Integer type, long maxValue, long minValue) {
        Class<? extends RoleMatcher> clazz = roleMatcherMap.get(type);
        if (clazz != null) {
            try {
                Constructor<? extends RoleMatcher> constructor = clazz.getConstructor(Integer.class, Long.class, Long.class);
                RoleMatcher roleMatcher = constructor.newInstance(type, maxValue, minValue);
                return roleMatcher;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
