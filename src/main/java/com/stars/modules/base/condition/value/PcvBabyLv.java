package com.stars.modules.base.condition.value;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.role.RoleModule;

import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/8/18.
 */
public class PcvBabyLv extends BaseExprValue {

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        int babylevel = roleModule.getBabyLevel();
        return (long) babylevel;
    }

    @Override
    public <T> T module(Map<String, Module> moduleMap, String moduleName) {
        return super.module(moduleMap, moduleName);
    }

    @Override
    public String toString() {
        return "宝宝等级";
    }
}
