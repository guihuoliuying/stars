package com.stars.core.expr.node.value.impl;

import com.stars.core.expr.node.value.ExprValue;
import com.stars.core.module.Module;
import com.stars.modules.data.DataManager;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PcvServerDays extends ExprValue {

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        return (long) DataManager.getServerDays();
    }

    @Override
    public String toString() {
        return "开服时间";
    }
}
