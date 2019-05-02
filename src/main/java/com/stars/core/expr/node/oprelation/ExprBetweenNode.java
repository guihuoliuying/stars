package com.stars.core.expr.node.oprelation;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.ExprContext;
import com.stars.core.expr.node.ExprNode;

import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class ExprBetweenNode extends ExprNode {

    private ExprNode n;
    private ExprNode rl;
    private ExprNode rr;

    public ExprBetweenNode(ExprConfig config, ExprNode n, ExprNode rl, ExprNode rr) {
        super(config);
        this.n = n;
        this.rl = rl;
        this.rr = rr;
    }

    @Override
    public Object eval(Object obj, ExprContext ctx) {
        long nv = (long) n.eval(obj, null);
        long rln = (long) rl.eval(obj, null);
        long rrn = (long) rr.eval(obj, null);
        if (rln > rrn) throw new IllegalStateException("eval error");

        long result = (long) (nv >= rln && nv <= rrn ? 1 : 0);
        // push the failure message
        if (result == 0) {
            ctx.getFalseStack().push(new LinkedHashSet<>(Arrays.asList(this)));
        }
        return result;
    }

    @Override
    public String inorderString() {
        return String.format("(%s,%s,%s,%s)", "Between",
                n.inorderString(), rl.inorderString(), rr.inorderString());
    }

    public ExprNode getChild() {
        return n;
    }

    public ExprNode getRangeLeft() {
        return rl;
    }

    public ExprNode getRangeRight() {
        return rr;
    }
}
