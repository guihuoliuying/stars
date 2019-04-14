package com.stars.modules.base.condition.dataset;

import com.stars.core.expr.node.dataset.ExprDataSet;
import com.stars.core.module.Module;

import java.util.Map;

public abstract class BaseExprDataSet extends ExprDataSet {

    private Map<String, Module> moduleMap;

    public BaseExprDataSet() {

    }

    public BaseExprDataSet(Map<String, Module> moduleMap) {
        this.moduleMap = moduleMap;
    }

    protected <T> T module(String name) {
        return (T) moduleMap.get(name);
    }

}
