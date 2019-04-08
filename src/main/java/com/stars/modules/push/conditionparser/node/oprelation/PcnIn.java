package com.stars.modules.push.conditionparser.node.oprelation;

import com.stars.core.module.Module;
import com.stars.modules.push.conditionparser.node.PushCondNode;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcnIn extends PushCondNode {

    private PushCondNode n;
    private List<PushCondNode> el;

    public PcnIn(PushCondNode n, List<PushCondNode> el) {
        this.n = n;
        this.el = el;
    }

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        long nv = (long) n.eval(moduleMap);
        for (PushCondNode e : el) {
            long ev = (long) e.eval(moduleMap);
            if (ev == nv) return (long) 1;
        }
        return (long) 0;
    }
}
