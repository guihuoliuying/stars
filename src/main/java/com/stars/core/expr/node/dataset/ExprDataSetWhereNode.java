package com.stars.core.expr.node.dataset;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public interface ExprDataSetWhereNode {

    String getFieldName();

    boolean eval(ExprData data, Object obj);

}
