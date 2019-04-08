package com.stars.modules.push.conditionparser.node.value.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.push.conditionparser.node.value.PushCondValue;
import com.stars.modules.role.RoleModule;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/6/22.
 */
public class PcvIsWxBinded extends PushCondValue {
    @Override
    public Object eval(Map<String, Module> moduleMap) {
    	RoleModule module = (RoleModule)moduleMap.get(MConst.Role);
        return (long)module.getByte("email_award_weixin");
    }

    @Override
    public String toString() {
        return "";
    }
}
