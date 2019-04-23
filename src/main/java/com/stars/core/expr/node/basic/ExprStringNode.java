package com.stars.core.expr.node.basic;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.node.ExprNode;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class ExprStringNode extends ExprNode {

    private String str;

    public ExprStringNode(ExprConfig config, String str) {
        super(config);
        this.str = str;
    }

    @Override
    public Object eval(Object obj) {
        return str;
    }

    @Override
    public String inorderString() {
        return String.format("(%s,%s)", "string", str);
    }
}
