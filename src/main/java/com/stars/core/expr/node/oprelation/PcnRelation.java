package com.stars.core.expr.node.oprelation;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.node.ExprNode;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcnRelation extends ExprNode {

    private ExprNode l;
    private ExprNode r;
    private String op;

    public PcnRelation(ExprConfig config, ExprNode l, ExprNode r, String op) {
        super(config);
        this.l = l;
        this.r = r;
        this.op = op;
    }

    @Override
    public Object eval(Object obj) {
        long lv = (long) l.eval(obj);
        long rv = (long) r.eval(obj);
        switch (op) {
            case ">": return (long) (lv > rv ? 1 : 0);
            case ">=": return (long) (lv >= rv ? 1 : 0);
            case "<": return (long) (lv < rv ? 1 : 0);
            case "<=": return (long) (lv <= rv ? 1 : 0);
            case "==": return (long) (lv == rv ? 1 : 0);
            case "!=": return (long) (lv != rv ? 1 : 0);
        }
        throw new IllegalStateException("eval error");
    }

    public ExprNode left() {
        return l;
    }

    public ExprNode right() {
        return r;
    }

    public String operator() {
        return op;
    }

}
