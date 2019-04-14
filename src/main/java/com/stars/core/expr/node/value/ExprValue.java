package com.stars.core.expr.node.value;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public abstract class ExprValue {

    public abstract Object eval(Object obj);

    public abstract String toString();
}
