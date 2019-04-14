package com.stars.core.expr.node.oprelation;

import com.stars.core.expr.node.ExprNode;
import com.stars.core.module.Module;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class ExprBetweenNode extends ExprNode {

    private ExprNode n;
    private ExprNode rl;
    private ExprNode rr;

    public ExprBetweenNode(ExprNode n, ExprNode rl, ExprNode rr) {
        this.n = n;
        this.rl = rl;
        this.rr = rr;
    }

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        long nv = (long) n.eval(moduleMap);
        long rln = (long) rl.eval(moduleMap);
        long rrn = (long) rr.eval(moduleMap);
        if (rln > rrn) throw new IllegalStateException("eval error");
        return (long) (nv >= rln && nv <= rrn ? 1 : 0);
    }

}
