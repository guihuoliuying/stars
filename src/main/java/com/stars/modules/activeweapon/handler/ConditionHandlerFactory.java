package com.stars.modules.activeweapon.handler;

import com.stars.core.module.Module;
import com.stars.modules.activeweapon.prodata.ActiveWeaponVo;
import com.stars.util.LogUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/6/15.
 */
public class ConditionHandlerFactory {
    static Map<Integer, Class<? extends ConditionHandler>> conditionHandlerMap = new HashMap<>();

    static {
        conditionHandlerMap.put(new ConditionHandler1().getType(), ConditionHandler1.class);
        conditionHandlerMap.put(new ConditionHandler2().getType(), ConditionHandler2.class);
        conditionHandlerMap.put(new ConditionHandler3().getType(), ConditionHandler3.class);
    }

    public static ConditionHandler newConditionHandler(Integer type, Map<String, Module> moduleMap, ActiveWeaponVo activeWeaponVo) {
        Class<? extends ConditionHandler> clazz = conditionHandlerMap.get(type);
        try {
            ConditionHandler conditionHandler = clazz.getConstructor(Map.class, ActiveWeaponVo.class).newInstance(moduleMap, activeWeaponVo);
            return conditionHandler;
        } catch (InstantiationException e) {
            com.stars.util.LogUtil.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            com.stars.util.LogUtil.error(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            com.stars.util.LogUtil.error(e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            LogUtil.error(e.getMessage(), e);
        }
        return null;
    }
}
