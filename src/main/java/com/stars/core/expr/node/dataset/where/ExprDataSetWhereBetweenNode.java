package com.stars.core.expr.node.dataset.where;

import com.stars.core.expr.node.ExprNode;
import com.stars.core.expr.node.dataset.ExprData;
import com.stars.core.expr.node.dataset.ExprDataSetWhereNode;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class ExprDataSetWhereBetweenNode implements ExprDataSetWhereNode {

    private String fieldName;
    private ExprNode rl;
    private ExprNode rr;

    public ExprDataSetWhereBetweenNode(String fieldName, ExprNode rl, ExprNode rr) {
        this.fieldName = fieldName;
        this.rl = rl;
        this.rr = rr;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public boolean eval(ExprData data, Object obj) {
        long fv = data.getField(fieldName);
        long rln = (long) rl.eval(obj, null);
        long rrn = (long) rr.eval(obj, null);
        if (rln > rrn) throw new IllegalStateException("eval error");
        return fv >= rln && fv <= rrn ? true : false;
    }
}
