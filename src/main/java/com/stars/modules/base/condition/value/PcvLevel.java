package com.stars.modules.base.condition.value;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.role.RoleModule;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcvLevel extends BaseExprValue {
    @Override
    public Object eval(Map<String, Module> moduleMap) {
        RoleModule roleModule = module(moduleMap, MConst.Role);
        return (long) roleModule.getLevel();
    }

    @Override
    public String toString() {
        return "等级";
    }
}
