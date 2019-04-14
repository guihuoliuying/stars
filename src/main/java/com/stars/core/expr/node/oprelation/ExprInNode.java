package com.stars.core.expr.node.oprelation;

import com.stars.core.expr.node.ExprNode;
import com.stars.core.module.Module;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class ExprInNode extends ExprNode {

    private ExprNode n;
    private List<ExprNode> el;

    public ExprInNode(ExprNode n, List<ExprNode> el) {
        this.n = n;
        this.el = el;
    }

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        long nv = (long) n.eval(moduleMap);
        for (ExprNode e : el) {
            long ev = (long) e.eval(moduleMap);
            if (ev == nv) return (long) 1;
        }
        return (long) 0;
    }
}
