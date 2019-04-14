package com.stars.modules.base.condition.value;

import com.stars.core.expr.node.value.ExprValue;
import com.stars.core.module.Module;

import java.util.Map;

public abstract class BaseExprValue extends ExprValue {

    @Override
    public Object eval(Object obj) {
        return eval((Map<String, Module>) obj);
    }

    public abstract Object eval(Map<String, Module> moduleMap);

    protected <T> T module(Map<String, Module> moduleMap, String moduleName) {
        return (T) moduleMap.get(moduleName);
    }
}
