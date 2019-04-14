package com.stars.core.expr.node.dataset.where;

import com.stars.core.expr.node.ExprNode;
import com.stars.core.expr.node.dataset.PushCondData;
import com.stars.core.expr.node.dataset.PushCondDataSetWhereNode;
import com.stars.core.module.Module;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcdswnIn implements PushCondDataSetWhereNode {

    private String fieldName;
    private List<ExprNode> el;

    public PcdswnIn(String fieldName, List<ExprNode> el) {
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
        for (ExprNode e : el) {
            long ev = (long) e.eval(moduleMap);
            if (ev == fv) return true;
        }
        return false;
    }
}
