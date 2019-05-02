package com.stars.core.expr.node.oprelation;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.ExprContext;
import com.stars.core.expr.node.ExprBinaryNode;
import com.stars.core.expr.node.ExprNode;

import java.util.Arrays;
import java.util.LinkedHashSet;

public abstract class ExprBinaryRelationNode extends ExprBinaryNode {

    public ExprBinaryRelationNode(ExprConfig config, ExprNode l, ExprNode r) {
        super(config, l, r);
    }

    @Override
    public Object eval(Object obj, ExprContext ctx) {
        long result = (long) eval0(obj, ctx);
        if (result == 0) {
            ctx.getFalseStack().push(new LinkedHashSet<>(Arrays.asList(this)));
        }
        return result;
    }

    public abstract Object eval0(Object obj, ExprContext ctx);

    public ExprNode getLeft() {
        return l;
    }

    public ExprNode getRight() {
        return r;
    }
}
