package com.stars.core.expr.node.dataset.where;

import com.stars.core.expr.node.ExprNode;
import com.stars.core.expr.node.dataset.ExprData;
import com.stars.core.expr.node.dataset.ExprDataSetWhereNode;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class ExprDataSetWhereRelationNode implements ExprDataSetWhereNode {

    private String fieldName;
    private ExprNode r;
    private String op;

    public ExprDataSetWhereRelationNode(String fieldName, ExprNode r, String op) {
        this.fieldName = fieldName;
        this.r = r;
        this.op = op;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public boolean eval(ExprData data, Object obj) {
        long fv = data.getField(fieldName);
        long rv = (long) r.eval(obj);
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
