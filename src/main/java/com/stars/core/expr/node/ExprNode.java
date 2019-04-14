package com.stars.core.expr.node;

import com.stars.core.expr.ExprConfig;

/**
 * Created by zhaowenshuo on 2017/3/22.
 */
public abstract class ExprNode {

    protected final ExprConfig config;

    public ExprNode(ExprConfig config) {
        this.config = config;
    }

    public abstract Object eval(Object obj);

}
