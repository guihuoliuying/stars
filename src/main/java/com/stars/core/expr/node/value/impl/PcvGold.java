package com.stars.core.expr.node.value.impl;

import com.stars.core.expr.node.value.ExprValue;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PcvGold extends ExprValue {

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        ToolModule toolModule = module(moduleMap, MConst.Tool);
        return (long) toolModule.getCountByItemId(ToolManager.GOLD);
    }

    @Override
    public String toString() {
        return "元宝";
    }
}
