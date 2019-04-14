package com.stars.core.expr.node.oprelation;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.node.ExprNode;

import java.util.List;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class ExprInNode extends ExprNode {

    private ExprNode n;
    private List<ExprNode> el;

    public ExprInNode(ExprConfig config, ExprNode n, List<ExprNode> el) {
        super(config);
        this.n = n;
        this.el = el;
    }

    @Override
    public Object eval(Object obj) {
        long nv = (long) n.eval(obj);
        for (ExprNode e : el) {
            long ev = (long) e.eval(obj);
            if (ev == nv) return (long) 1;
        }
        return (long) 0;
    }
}
