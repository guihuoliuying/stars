package com.stars.core.expr.node;

import com.stars.core.expr.ExprConfig;

public abstract class ExprExtensionNode extends ExprNode {

    protected String name;

    public ExprExtensionNode(ExprConfig config, String name) {
        super(config);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
