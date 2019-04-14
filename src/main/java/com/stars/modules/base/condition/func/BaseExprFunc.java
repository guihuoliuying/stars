package com.stars.modules.base.condition.func;

import com.stars.core.expr.node.func.ExprFunc;
import com.stars.core.module.Module;

import java.util.List;
import java.util.Map;

public abstract class BaseExprFunc extends ExprFunc {

    @Override
    public Object eval(Object obj, List<Object> paramList) {
        return eval((Map<String, Module>) obj, paramList);
    }

    public abstract Object eval(Map<String, Module> moduleMap, List<Object> paramList);

    protected <T> T module(Map<String, Module> moduleMap, String moduleName) {
        return (T) moduleMap.get(moduleName);
    }
}
