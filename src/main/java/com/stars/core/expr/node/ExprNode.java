package com.stars.core.expr.node;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.ExprContext;

/**
 * Created by zhaowenshuo on 2017/3/22.
 */
public abstract class ExprNode {

    protected final ExprConfig config;

    public ExprNode(ExprConfig config) {
        this.config = config;
    }

    public abstract Object eval(Object obj, ExprContext ctx);

    public Object eval(Object obj) {
        return eval(obj, new ExprContext());
    }

    public Object eval() {
        return eval(null, new ExprContext());
    }

    public String inorderString() {
        return "";
    }

}
