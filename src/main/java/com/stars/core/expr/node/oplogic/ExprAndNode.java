package com.stars.core.expr.node.oplogic;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.ExprContext;
import com.stars.core.expr.node.ExprBinaryNode;
import com.stars.core.expr.node.ExprNode;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class ExprAndNode extends ExprBinaryNode {

    public ExprAndNode(ExprConfig config, ExprNode l, ExprNode r) {
        super(config, l, r);
    }

    @Override
    public Object eval(Object obj, ExprContext ctx) {
        // left
        long lv = (long) l.eval(obj, ctx);
        if (lv == 0) {
            if (ctx.getFalseStack().size() > 0) {
                ctx.getFalseStack().push(ctx.getFalseStack().pop());
            }
            return (long) 0;
        }
        // right
        long rv = (long) r.eval(obj, ctx);
        if (rv == 0) {
            if (ctx.getFalseStack().size() > 0) {
                ctx.getFalseStack().push(ctx.getFalseStack().pop());
            }
            return (long) 0;
        }
        return (long) 1;
    }

    @Override
    protected String operatorName() {
        return "And";
    }

}
