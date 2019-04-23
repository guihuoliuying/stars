package com.stars.core.expr.node.basic;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.node.ExprNode;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class ExprDigitsNode extends ExprNode {

    private long digits;

    public ExprDigitsNode(ExprConfig config, String str) {
        super(config);
        this.digits = Long.parseLong(str);
    }

    @Override
    public Object eval(Object obj) {
        return digits;
    }

    @Override
    public String toString() {
        return Long.toString(digits);
    }

    @Override
    public String inorderString() {
        return String.format("(%s,%d)", "digits", digits);
    }
}
