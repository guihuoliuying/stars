package com.stars.core.expr.node.oparith;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.node.ExprNode;
import com.stars.core.module.Module;

import java.util.Map;

public class ExprPowNode extends ExprNode {

    private ExprNode l;
    private ExprNode r;

    public ExprPowNode(ExprConfig config, ExprNode l, ExprNode r) {
        super(config);
        this.l = l;
        this.r = r;
    }

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        return (long) Math.pow((long) l.eval(moduleMap), (long) r.eval(moduleMap));
    }
}
