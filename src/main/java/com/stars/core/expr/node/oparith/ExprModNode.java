package com.stars.core.expr.node.oparith;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.node.ExprNode;

public class ExprModNode extends ExprNode {

    private ExprNode l;
    private ExprNode r;

    public ExprModNode(ExprConfig config, ExprNode l, ExprNode r) {
        super(config);
        this.l = l;
        this.r = r;
    }

    @Override
    public Object eval(Object obj) {
        return (long) l.eval(obj) % (long) r.eval(obj);
    }

    @Override
    public String inorderString() {
        return String.format("(%s,%s,%s)", "mod", l.inorderString(), r.inorderString());
    }
}
