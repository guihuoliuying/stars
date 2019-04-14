package com.stars.core.expr.node.value.impl;

import com.stars.core.expr.node.value.ExprValue;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/4/2.
 */
public class PcvVigor extends ExprValue {

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        ToolModule toolModule = module(moduleMap, MConst.Tool);
        return (long) toolModule.getCountByItemId(ToolManager.VIGOR);
    }

    @Override
    public String toString() {
        return "体力";
    }
}
