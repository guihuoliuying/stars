package com.stars.core.expr.node.func;

import com.stars.core.module.Module;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public abstract class ExprFunc {

    public abstract Object eval(Map<String, Module> moduleMap, List<Object> paramList);

    public <T> T module(Map<String, Module> moduleMap, String moduleName) {
        return (T) moduleMap.get(moduleName);
    }

}
