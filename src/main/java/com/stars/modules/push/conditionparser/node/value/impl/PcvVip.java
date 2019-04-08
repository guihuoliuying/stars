package com.stars.modules.push.conditionparser.node.value.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.push.conditionparser.node.value.PushCondValue;
import com.stars.modules.vip.VipModule;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcvVip extends PushCondValue {
    @Override
    public Object eval(Map<String, Module> moduleMap) {
        VipModule vipModule = module(moduleMap, MConst.Vip);
        return (long) vipModule.getVipLevel();
    }

    @Override
    public String toString() {
        return "贵族等级";
    }
}
