package com.stars.core.expr.node.oplogic;

import com.stars.core.expr.node.ExprNode;
import com.stars.core.module.Module;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class ExprNotNode extends ExprNode {

    private ExprNode n;

    public ExprNotNode(ExprNode n) {
        this.n = n;
    }

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        long v = (long) n.eval(moduleMap);
        return (long) (v != 0 ? 0 : 1);
    }
}
