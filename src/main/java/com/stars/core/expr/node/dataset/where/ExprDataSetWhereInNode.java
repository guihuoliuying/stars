package com.stars.core.expr.node.dataset.where;

import com.stars.core.expr.node.ExprNode;
import com.stars.core.expr.node.dataset.ExprData;
import com.stars.core.expr.node.dataset.ExprDataSetWhereNode;

import java.util.List;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class ExprDataSetWhereInNode implements ExprDataSetWhereNode {

    private String fieldName;
    private List<ExprNode> el;

    public ExprDataSetWhereInNode(String fieldName, List<ExprNode> el) {
        this.fieldName = fieldName;
        this.el = el;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public boolean eval(ExprData data, Object obj) {
        long fv = data.getField(fieldName);
        for (ExprNode e : el) {
            long ev = (long) e.eval(obj);
            if (ev == fv) return true;
        }
        return false;
    }
}
