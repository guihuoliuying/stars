package com.stars.core.expr.example.extensions.function;

import com.stars.core.expr.node.func.ExprFunc;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ExampleEfRandom extends ExprFunc {

    @Override
    public Object eval(Object obj, List<Object> paramList) {
        Long start, end;
        if (paramList.size() == 0) {
            return ThreadLocalRandom.current().nextLong();
        }
        start = (Long) paramList.get(0);
        if (paramList.size() == 1) {
            return ThreadLocalRandom.current().nextLong(start);
        }
        if (paramList.size() == 2) {
            end = (Long) paramList.get(1);
            return ThreadLocalRandom.current().nextLong(start, end);
        }
        throw new IllegalArgumentException();
    }
}
