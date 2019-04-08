package com.stars.modules.push.conditionparser.node.dataset.where;

import com.stars.core.module.Module;
import com.stars.modules.push.conditionparser.node.PushCondNode;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSetWhereNode;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcdswnRelation implements PushCondDataSetWhereNode {

    private String fieldName;
    private PushCondNode r;
    private String op;

    public PcdswnRelation(String fieldName, PushCondNode r, String op) {
        this.fieldName = fieldName;
        this.r = r;
        this.op = op;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public boolean eval(PushCondData data, Map<String, Module> moduleMap) {
        long fv = data.getField(fieldName);
        long rv = (long) r.eval(moduleMap);
        switch (op) {
            case ">": return fv > rv;
            case ">=": return fv >= rv;
            case "<": return fv < rv;
            case "<=": return fv <= rv;
            case "==": return fv == rv;
            case "!=": return fv != rv;
        }
        throw new IllegalStateException("eval error");
    }
}
