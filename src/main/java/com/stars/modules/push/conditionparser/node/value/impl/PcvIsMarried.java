package com.stars.modules.push.conditionparser.node.value.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.marry.MarryModule;
import com.stars.modules.push.conditionparser.node.value.PushCondValue;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/6/22.
 */
public class PcvIsMarried extends PushCondValue {

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        MarryModule module = module(moduleMap, MConst.Marry);
        return module.isMarried() ? 1L : 0L;
    }

    @Override
    public String toString() {
        return "已婚";
    }
}
