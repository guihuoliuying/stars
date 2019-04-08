package com.stars.modules.push.conditionparser.node.oplogic;

import com.stars.core.module.Module;
import com.stars.modules.push.conditionparser.node.PushCondNode;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcnNot extends PushCondNode {

    private PushCondNode n;

    public PcnNot(PushCondNode n) {
        this.n = n;
    }

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        long v = (long) n.eval(moduleMap);
        return (long) (v != 0 ? 0 : 1);
    }
}
