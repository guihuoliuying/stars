package com.stars.core.expr.node.oplogic;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.node.ExprNode;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class ExprAndNode extends ExprNode {

    private ExprNode l;
    private ExprNode r;

    public ExprAndNode(ExprConfig config, ExprNode l, ExprNode r) {
        super(config);
        this.l = l;
        this.r = r;
    }

    @Override
    public Object eval(Object obj) {
        long lv = (long) l.eval(obj);
        long rv = (long) r.eval(obj);
        return (long) ((lv != 0 && rv != 0) ? 1 : 0);
    }

    @Override
    public String inorderString() {
        return String.format("(%s,%s,%s)", "and", l.inorderString(), r.inorderString());
    }
}