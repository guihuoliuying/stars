package com.stars.core.expr.node;

import com.stars.core.expr.ExprConfig;
import com.stars.core.module.Module;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/22.
 */
public abstract class ExprNode {

    protected ExprConfig config;

    public ExprNode() {
    }

    public ExprNode(ExprConfig config) {
        this.config = config;
    }

    public abstract Object eval(Map<String, Module> moduleMap);

}
