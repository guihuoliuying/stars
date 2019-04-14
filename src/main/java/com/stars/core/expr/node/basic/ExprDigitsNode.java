package com.stars.core.expr.node.basic;

import com.stars.core.expr.node.ExprNode;
import com.stars.core.module.Module;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class ExprDigitsNode extends ExprNode {

    private long digits;

    public ExprDigitsNode(String str) {
        this.digits = Long.parseLong(str);
    }

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        return digits;
    }

    @Override
    public String toString() {
        return Long.toString(digits);
    }
}
