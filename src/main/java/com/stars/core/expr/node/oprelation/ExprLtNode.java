package com.stars.core.expr.node.oprelation;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.ExprContext;
import com.stars.core.expr.node.ExprNode;

public class ExprLtNode extends ExprBinaryRelationNode {

    public ExprLtNode(ExprConfig config, ExprNode l, ExprNode r) {
        super(config, l, r);
    }

    @Override
    public Object eval0(Object obj, ExprContext ctx) {
        return (long) ((long) l.eval(obj, ctx) < (long) r.eval(obj, ctx) ? 1 : 0);
    }

    @Override
    protected String operatorName() {
        return "<";
    }
}
