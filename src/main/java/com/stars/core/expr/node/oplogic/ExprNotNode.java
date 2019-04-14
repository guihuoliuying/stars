package com.stars.core.expr.node.oplogic;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.node.ExprNode;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class ExprNotNode extends ExprNode {

    private ExprNode n;

    public ExprNotNode(ExprConfig config, ExprNode n) {
        super(config);
        this.n = n;
    }

    @Override
    public Object eval(Object obj) {
        long v = (long) n.eval(obj);
        return (long) (v != 0 ? 0 : 1);
    }
}
