package com.stars.modules.push.conditionparser.node.value.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.chargepreference.ChargePrefModule;
import com.stars.modules.push.conditionparser.node.value.PushCondValue;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/6/22.
 */
public class PcvChargePrefChosenId extends PushCondValue {

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        ChargePrefModule module = module(moduleMap, MConst.ChargePref);
        return (long) module.getChosenPrefId();
    }

    @Override
    public String toString() {
        return "";
    }
}
