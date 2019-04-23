package com.stars.core.expr;

import com.stars.core.expr.node.ExprNode;

import java.util.concurrent.ConcurrentMap;

public class ExprFactory {

    private ExprConfig config;
    private ConcurrentMap<String, ExprNode> cache;

    public ExprFactory() {
        this.config = new ExprConfig();
    }

    public ExprFactory(ExprConfig config) {
        this.config = config;
    }

    public ExprNode create(String exprString) {
        if (cache.containsKey(exprString)) {
            return cache.get(exprString);
        } else {
            return cache.computeIfAbsent(
                    exprString, s -> new ExprParser(new ExprLexer(exprString), config).parse());
        }
    }
}
