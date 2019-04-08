package com.stars.util;

import org.keplerproject.luajava.LuaObject;
import org.keplerproject.luajava.LuaState;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by Garwah on 2015/12/30.
 */
public class LuaUtil {
    /**
     * 处理java到lua的一些方法
     * 为了防止调用此类的方法遇到异常,有返回的内容如果遇到异常会返回null
     */

    /**
     * 拿到lua的类
     */
    public static LuaObject getLuaFile(LuaState luaState, String fileName) {
        if (luaState == null || fileName == null) {
            return null;
        }
        try {
            return luaState.getLuaObject(fileName);
        } catch (Exception e) {
            LogUtil.error("调用lua类异常,类名:" + fileName, e);
            return null;
        }
    }

    /**
     * 拿到lua的方法
     * 其实只是getField出来...
     *
     * @return
     */
    public static LuaObject getLuaFunc(LuaState luaState, String fileName, String funcName) {
        if (luaState == null || fileName == null || funcName == null) {
            return null;
        }
        try {
            LuaObject luaFile = getLuaFile(luaState, fileName);
            if (luaFile == null) {
                return null;
            }
            return luaFile.getField(funcName);
        } catch (Exception e) {
            LogUtil.error("调用lua类异常,类名:" + fileName, e);
            return null;
        }
    }

    public static Object[] call(LuaState luaState, String methodName, int nres, Object ... args) {
        try {
            Objects.requireNonNull(luaState);
            Objects.requireNonNull(methodName);
            String[] paths = methodName.split("\\.");
            LuaObject lobj = luaState.getLuaObject(paths[0]);
            for (int i = 1; i < paths.length; i++) {
                lobj = luaState.getLuaObject(lobj, paths[i]);
            }
            return args != null ? lobj.call(args, nres) : lobj.call(new Object[]{}, nres);
        } catch (Throwable cause) {
            LogUtil.error("调用lua异常|" + methodName + "(" + Arrays.toString(args) + ")", cause);
            return null;
        }
    }

    public static Object call(LuaState luaState, String methodName, Object ... args) {
        return call(luaState, methodName, 1, args)[0];
    }

    public static void callNoResult(LuaState luaState, String methodName, Object ... args) {
        call(luaState, methodName, 0, args);
    }

}
