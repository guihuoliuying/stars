package com.stars.modules.activeweapon.handler;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.activeweapon.ActiveWeaponModule;
import com.stars.modules.activeweapon.prodata.ActiveWeaponVo;
import com.stars.modules.activeweapon.usrdata.RoleActiveWeapon;

import java.util.Map;
import java.util.Set;

/**
 * Created by huwenjun on 2017/6/15.
 */
public class ConditionHandler1 extends ConditionHandler {
    public ConditionHandler1(Map<String, Module> moduleMap, ActiveWeaponVo activeWeaponVo) {
        super(moduleMap, activeWeaponVo);
    }

    public ConditionHandler1() {
    }

    @Override
    public boolean check() {
        ActiveWeaponModule activeWeaponModule = (ActiveWeaponModule) moduleMap.get(MConst.ActiveWeapon);
        RoleActiveWeapon roleActiveWeapon = activeWeaponModule.getRoleActiveWeapon();
        Set<String> onlineDaySet = roleActiveWeapon.getOnlineDaySet();
        String condition = activeWeaponVo.getCondition();
        int needDays = Integer.parseInt(condition);
        return onlineDaySet.size() >= needDays;
    }

    @Override
    public Integer getType() {
        return 1;
    }
}
