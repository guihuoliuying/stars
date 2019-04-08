package com.stars.modules.push.conditionparser.node.oplogic;

import com.stars.core.module.Module;
import com.stars.modules.push.conditionparser.node.PushCondNode;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcnAnd extends PushCondNode {

    private PushCondNode l;
    private PushCondNode r;

    public PcnAnd(PushCondNode l, PushCondNode r) {
        this.l = l;
        this.r = r;
    }

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        long lv = (long) l.eval(moduleMap);
        long rv = (long) r.eval(moduleMap);
        return (long) ((lv != 0 && rv != 0) ? 1 : 0);
    }
}
