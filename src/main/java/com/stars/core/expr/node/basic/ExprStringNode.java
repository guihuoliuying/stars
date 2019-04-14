package com.stars.core.expr.node.basic;

import com.stars.core.expr.node.ExprNode;
import com.stars.core.module.Module;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class ExprStringNode extends ExprNode {

    private String str;

    public ExprStringNode(String str) {
        this.str = str;
    }

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        return str;
    }
}
