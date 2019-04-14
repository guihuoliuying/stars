package com.stars.core.expr.node.oprelation;

import com.stars.core.expr.node.ExprNode;
import com.stars.core.module.Module;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcnRelation extends ExprNode {

    private ExprNode l;
    private ExprNode r;
    private String op;

    public PcnRelation(ExprNode l, ExprNode r, String op) {
        this.l = l;
        this.r = r;
        this.op = op;
    }

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        long lv = (long) l.eval(moduleMap);
        long rv = (long) r.eval(moduleMap);
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
