package com.stars.modules.push.conditionparser.node.oprelation;

import com.stars.core.module.Module;
import com.stars.modules.push.conditionparser.node.PushCondNode;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcnRelation extends PushCondNode {

    private PushCondNode l;
    private PushCondNode r;
    private String op;

    public PcnRelation(PushCondNode l, PushCondNode r, String op) {
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

    public PushCondNode left() {
        return l;
    }

    public PushCondNode right() {
        return r;
    }

    public String operator() {
        return op;
    }

}
