package com.stars.core.expr.node.oparith;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.node.ExprNode;

public class ExprPowNode extends ExprNode {

    private ExprNode l;
    private ExprNode r;

    public ExprPowNode(ExprConfig config, ExprNode l, ExprNode r) {
        super(config);
        this.l = l;
        this.r = r;
    }

    @Override
    public Object eval(Object obj) {
        return (long) Math.pow((long) l.eval(obj), (long) r.eval(obj));
    }

    @Override
    public String inorderString() {
        return String.format("(%s,%s,%s)", "pow", l.inorderString(), r.inorderString());
    }
}
