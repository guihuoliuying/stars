package com.stars.core.expr;

import com.stars.core.expr.node.ExprNode;

import java.util.Set;
import java.util.Stack;

public class ExprContext {

    private Stack<Set<ExprNode>> falseStack;

    public ExprContext() {
        this.falseStack = new Stack<>();
    }

    public Stack<Set<ExprNode>> getFalseStack() {
        return falseStack;
    }
}
