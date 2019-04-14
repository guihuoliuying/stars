package com.stars.core.expr.node.oparith;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.node.ExprNode;

public class ExprMulNode extends ExprNode {

    private ExprNode l;
    private ExprNode r;

    public ExprMulNode(ExprConfig config, ExprNode l, ExprNode r) {
        super(config);
        this.l = l;
        this.r = r;
    }

    @Override
    public Object eval(Object obj) {
        return (long) l.eval(obj) * (long) r.eval(obj);
    }
}
