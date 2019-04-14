package com.stars.core.expr.node.value.impl;

import com.stars.core.expr.node.value.ExprValue;
import com.stars.core.module.Module;
import com.stars.multiserver.MultiServerHelper;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PcvServerId extends ExprValue {

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        return (long) MultiServerHelper.getDisplayServerId();
    }

    @Override
    public String toString() {
        return "服务编号";
    }
}
