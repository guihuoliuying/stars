package com.stars.core.expr.node.oplogic;

import com.stars.core.expr.node.ExprNode;
import com.stars.core.module.Module;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class ExprOrNode extends ExprNode {

    private ExprNode l;
    private ExprNode r;

    public ExprOrNode(ExprNode l, ExprNode r) {
        this.l = l;
        this.r = r;
    }

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        long lv = (long) l.eval(moduleMap);
        long rv = (long) r.eval(moduleMap);
        return (long) ((lv != 0 || rv != 0) ? 1 : 0);
    }
}
