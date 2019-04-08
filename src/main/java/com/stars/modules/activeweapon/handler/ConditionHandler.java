package com.stars.modules.activeweapon.handler;

import com.stars.core.module.Module;
import com.stars.modules.activeweapon.prodata.ActiveWeaponVo;

import java.util.Map;

/**
 * Created by huwenjun on 2017/6/15.
 */
public abstract class ConditionHandler {
    protected Map<String, Module> moduleMap = null;
    protected ActiveWeaponVo activeWeaponVo = null;

    public ConditionHandler(Map<String, Module> moduleMap, ActiveWeaponVo activeWeaponVo) {
        this.moduleMap = moduleMap;
        this.activeWeaponVo = activeWeaponVo;
    }

    public ConditionHandler() {

    }

    public abstract boolean check();

    public abstract Integer getType();
}
