package com.stars.core.expr.node.oplogic;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.ExprContext;
import com.stars.core.expr.node.ExprNode;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class ExprNotNode extends ExprNode {

    private ExprNode n;

    public ExprNotNode(ExprConfig config, ExprNode n) {
        super(config);
        this.n = n;
    }

    @Override
    public Object eval(Object obj, ExprContext ctx) {
        long v = (long) n.eval(obj, null);
        if (v != 0) {
            if (ctx.getFalseStack().size() > 0) {
                ctx.getFalseStack().push(ctx.getFalseStack().pop());
            }
            return (long) 0;
        }
        return (long) 1;
    }

    @Override
    public String inorderString() {
        return String.format("(%s,%s)", "not", n.inorderString());
    }
}
