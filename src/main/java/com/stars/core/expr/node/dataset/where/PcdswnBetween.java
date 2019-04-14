package com.stars.core.expr.node.dataset.where;

import com.stars.core.expr.node.ExprNode;
import com.stars.core.expr.node.dataset.PushCondData;
import com.stars.core.expr.node.dataset.PushCondDataSetWhereNode;
import com.stars.core.module.Module;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcdswnBetween implements PushCondDataSetWhereNode {

    private String fieldName;
    private ExprNode rl;
    private ExprNode rr;

    public PcdswnBetween(String fieldName, ExprNode rl, ExprNode rr) {
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
