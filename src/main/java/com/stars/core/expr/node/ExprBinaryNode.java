package com.stars.core.expr.node;

import com.stars.core.expr.ExprConfig;

public abstract class ExprBinaryNode extends ExprNode {

    protected ExprNode l;
    protected ExprNode r;

    public ExprBinaryNode(ExprConfig config, ExprNode l, ExprNode r) {
        super(config);
        this.l = l;
        this.r = r;
    }

    protected abstract String operatorName();

    @Override
    public String inorderString() {
        return String.format("(%s,%s,%s)", operatorName(), l.inorderString(), r.inorderString());
    }
}
