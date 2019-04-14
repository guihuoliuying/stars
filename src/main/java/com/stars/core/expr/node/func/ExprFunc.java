package com.stars.core.expr.node.func;

import java.util.List;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public abstract class ExprFunc {

    public abstract Object eval(Object obj, List<Object> paramList);

}
