package com.stars.modules.activeweapon.handler;

import com.stars.core.module.Module;
import com.stars.modules.activeweapon.ActiveWeaponManager;
import com.stars.modules.activeweapon.prodata.ActiveWeaponVo;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/6/15.
 */
public class ConditionHandler3 extends ConditionHandler {
    public ConditionHandler3(Map<String, Module> moduleMap, ActiveWeaponVo activeWeaponVo) {
        super(moduleMap, activeWeaponVo);
    }

    public ConditionHandler3() {
    }

    @Override
    public boolean check() {
        String condition = activeWeaponVo.getCondition();
        try {
            List<Integer> conditionIdList = StringUtil.toArrayList(condition, Integer.class, '+');
            for (Integer conditionId : conditionIdList) {
                ActiveWeaponVo activeWeaponVo = ActiveWeaponManager.activeWeaponVoMap.get(conditionId);
                ConditionHandler conditionHandler = ConditionHandlerFactory.newConditionHandler(activeWeaponVo.getType(), moduleMap, activeWeaponVo);
                boolean success = conditionHandler.check();
                if (!success) {
                    return false;
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        return true;
    }

    @Override
    public Integer getType() {
        return 3;
    }
}
