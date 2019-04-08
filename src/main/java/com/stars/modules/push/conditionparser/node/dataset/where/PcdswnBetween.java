package com.stars.modules.push.conditionparser.node.dataset.where;

import com.stars.core.module.Module;
import com.stars.modules.push.conditionparser.node.PushCondNode;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSetWhereNode;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcdswnBetween implements PushCondDataSetWhereNode {

    private String fieldName;
    private PushCondNode rl;
    private PushCondNode rr;

    public PcdswnBetween(String fieldName, PushCondNode rl, PushCondNode rr) {
        this.fieldName = fieldName;
        this.rl = rl;
        this.rr = rr;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public boolean eval(PushCondData data, Map<String, Module> moduleMap) {
        long fv = data.getField(fieldName);
        long rln = (long) rl.eval(moduleMap);
        long rrn = (long) rr.eval(moduleMap);
        if (rln > rrn) throw new IllegalStateException("eval error");
        return fv >= rln && fv <= rrn ? true : false;
    }
}
