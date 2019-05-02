package com.stars.core.expr;

import com.stars.core.expr.node.ExprNode;

import java.util.Set;
import java.util.Stack;

public class ExprContext {

    private ExprConfig config;
    private Stack<Set<ExprNode>> falseStack;

    public ExprContext(ExprConfig config) {
        this.config = config;
        this.falseStack = new Stack<>();
    }

    public ExprContext() {
        this.falseStack = new Stack<>();
    }

    public ExprConfig getConfig() {
        return config;
    }

    public Stack<Set<ExprNode>> getFalseStack() {
        return falseStack;
    }
}
