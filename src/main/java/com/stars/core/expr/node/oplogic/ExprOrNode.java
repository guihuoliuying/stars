package com.stars.core.expr.node.oplogic;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.ExprContext;
import com.stars.core.expr.node.ExprBinaryNode;
import com.stars.core.expr.node.ExprNode;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class ExprOrNode extends ExprBinaryNode {

    public ExprOrNode(ExprConfig config, ExprNode l, ExprNode r) {
        super(config, l, r);
    }

    @Override
    public Object eval(Object obj, ExprContext ctx) {
        long lv = (long) l.eval(obj, null);
        // short cut
        if (lv != 0) {
            return (long) 1;
        }
        long rv = (long) r.eval(obj, null);
        if (rv != 0) {
            return (long) 1;
        }
        // message of condition failure
        if (ctx != null) {
            Set<ExprNode> nodeSet = new LinkedHashSet<>();
            nodeSet.addAll(ctx.getFalseStack().pop());
            nodeSet.addAll(ctx.getFalseStack().pop());
            ctx.getFalseStack().push(nodeSet);
        }
        return (long) 0;
    }

    @Override
    protected String operatorName() {
        return "Or";
    }

}
