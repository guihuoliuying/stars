package com.stars.core.expr.example.extensions.value;

import com.stars.core.expr.node.value.ExprValue;

import java.util.Map;

public class ExampleEvVipLevel extends ExprValue {
    @Override
    public Object eval(Object obj) {
        return (Long) ((Map<String, Object>) obj).get("vipLevel");
    }

    @Override
    public String toString() {
        return null;
    }
}
