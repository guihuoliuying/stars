package com.stars.core.expr.node.dataset;

import com.stars.core.module.Module;

import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public abstract class PushCondDataSet {

    private Map<String, Module> moduleMap;

    public PushCondDataSet() {

    }

    public PushCondDataSet(Map<String, Module> moduleMap) {
        this.moduleMap = moduleMap;
    }

    protected <T> T module(String name) {
        return (T) moduleMap.get(name);
    }

    public abstract boolean hasNext();

    public abstract PushCondData next();

    public abstract Set<String> fieldSet();

}
