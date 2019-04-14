package com.stars.core.expr.node.oprelation;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.node.ExprNode;

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
    public Object eval(Object obj) {
        long nv = (long) n.eval(obj);
        long rln = (long) rl.eval(obj);
        long rrn = (long) rr.eval(obj);
        if (rln > rrn) throw new IllegalStateException("eval error");
        return (long) (nv >= rln && nv <= rrn ? 1 : 0);
    }

}
