package com.stars.core.expr.node.value.impl.fight;

import com.stars.core.expr.node.value.ExprValue;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.RoleModule;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/4/2.
 */
public class PcvFightLevel extends ExprValue {
    @Override
    public Object eval(Map<String, Module> moduleMap) {
        RoleModule roleModule = module(moduleMap, MConst.Role);
        return (long) roleModule.getRoleRow().getFightScoreMap().get(RoleManager.FIGHTSCORE_GRADE);
    }

    @Override
    public String toString() {
        return "渠道";
    }
}
