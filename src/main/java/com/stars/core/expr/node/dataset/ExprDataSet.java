package com.stars.core.expr.node.dataset;

import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public abstract class ExprDataSet {

    public ExprDataSet() {

    }

    public abstract boolean hasNext();

    public abstract ExprData next();

    public abstract Set<String> fieldSet();

}
