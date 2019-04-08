package com.stars.modules.push.conditionparser.node.value.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.discountgift.DiscountGiftModule;
import com.stars.modules.push.conditionparser.node.value.PushCondValue;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/6/22.
 */
public class PcvDiscountGiftGroupId extends PushCondValue {
    @Override
    public Object eval(Map<String, Module> moduleMap) {
        DiscountGiftModule module = module(moduleMap, MConst.DiscountGift);
        return (long) module.getGiftGroupId();
    }

    @Override
    public String toString() {
        return "";
    }
}
