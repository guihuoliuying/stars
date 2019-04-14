package com.stars.modules.base.condition.value.fight;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.base.condition.value.BaseExprValue;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.RoleModule;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/4/1.
 */
public class PcvFightGem extends BaseExprValue {
    @Override
    public Object eval(Map<String, Module> moduleMap) {
        RoleModule roleModule = module(moduleMap, MConst.Role);
        return (long) roleModule.getRoleRow().getFightScoreMap().get(RoleManager.FIGHTSCORE_GEM);
    }

    @Override
    public String toString() {
        return "渠道";
    }
}
