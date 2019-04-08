package com.stars.modules.push.conditionparser.node.dataset.where;

import com.stars.core.module.Module;
import com.stars.modules.push.conditionparser.node.PushCondNode;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSetWhereNode;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcdswnIn implements PushCondDataSetWhereNode {

    private String fieldName;
    private List<PushCondNode> el;

    public PcdswnIn(String fieldName, List<PushCondNode> el) {
        this.fieldName = fieldName;
        this.el = el;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public boolean eval(PushCondData data, Map<String, Module> moduleMap) {
        long fv = data.getField(fieldName);
        for (PushCondNode e : el) {
            long ev = (long) e.eval(moduleMap);
            if (ev == fv) return true;
        }
        return false;
    }
}
