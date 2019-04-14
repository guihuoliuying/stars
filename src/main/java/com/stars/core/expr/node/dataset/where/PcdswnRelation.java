package com.stars.core.expr.node.dataset.where;

import com.stars.core.expr.node.ExprNode;
import com.stars.core.expr.node.dataset.PushCondData;
import com.stars.core.expr.node.dataset.PushCondDataSetWhereNode;
import com.stars.core.module.Module;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcdswnRelation implements PushCondDataSetWhereNode {

    private String fieldName;
    private ExprNode r;
    private String op;

    public PcdswnRelation(String fieldName, ExprNode r, String op) {
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
