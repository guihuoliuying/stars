package com.stars.modules.push.conditionparser.node.value.impl.fight;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.push.conditionparser.node.value.PushCondValue;
import com.stars.modules.role.RoleModule;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/4/1.
 */
public class PcvFightFamilySkill extends PushCondValue {
    @Override
    public Object eval(Map<String, Module> moduleMap) {
        RoleModule roleModule = module(moduleMap, MConst.Role);
        return (long) roleModule.getRoleRow().getFightScoreMap().get("familySkill");
    }

    @Override
    public String toString() {
        return "渠道";
    }
}
