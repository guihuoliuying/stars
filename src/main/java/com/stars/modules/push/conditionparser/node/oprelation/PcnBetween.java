package com.stars.modules.push.conditionparser.node.oprelation;

import com.stars.core.module.Module;
import com.stars.modules.push.conditionparser.node.PushCondNode;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcnBetween extends PushCondNode {

    private PushCondNode n;
    private PushCondNode rl;
    private PushCondNode rr;

    public PcnBetween(PushCondNode n, PushCondNode rl, PushCondNode rr) {
        this.n = n;
        this.rl = rl;
        this.rr = rr;
    }

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        long nv = (long) n.eval(moduleMap);
        long rln = (long) rl.eval(moduleMap);
        long rrn = (long) rr.eval(moduleMap);
        if (rln > rrn) throw new IllegalStateException("eval error");
        return (long) (nv >= rln && nv <= rrn ? 1 : 0);
    }

}
