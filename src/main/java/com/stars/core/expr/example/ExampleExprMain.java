package com.stars.core.expr.example;

import com.stars.core.expr.ExprContext;
import com.stars.core.expr.ExprLexer;
import com.stars.core.expr.ExprParser;
import com.stars.core.expr.ExprUtil;
import com.stars.core.expr.node.ExprNode;

import java.util.HashMap;
import java.util.Map;

public class ExampleExprMain {
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("level", 10L);
        map.put("vipLevel", 1L);

        //
        separator("单值");
        calc("level", map);
        calc("vipLevel", map);

        separator("函数");
        calc("{random}");
        calc("{random, 10}");
        calc("{random, 10, 100}");

        //
        separator("运算");
        calc("1 + 1");
        calc("1 - 1");
        calc("1 - 2");
        calc("1 * 2");
        calc("1 / 2");
        try {
            calc("1 / 0");
        } catch (ArithmeticException e) {
            e.printStackTrace();
        }
        calc("1 % 2");
        calc("2 ^ 10");
        calc("1 + 1 * 2");
        calc("(1 + 1) * 2");
        calc("1 * 2 + 1 * 2");
        calc("2 + 3 * 4 ^ 5");
        calc("((2 + 3) * 4) ^ 5");
        calc("level - vipLevel", map);
        calc("vipLevel - level", map);

        separator("条件");
        cond("1 == 1");
        cond("1 == 2");
        cond("1 != 1");
        cond("1 != 2");
        cond("1 > 0");
        cond("1 > 1");
        cond("1 > 2");
        cond("1 >= 0");
        cond("1 >= 1");
        cond("1 >= 2");
        cond("1 < 0");
        cond("1 < 1");
        cond("1 < 2");
        cond("1 <= 0");
        cond("1 <= 1");
        cond("1 <= 2");
        cond("1 in (1, 2, 3)");
        cond("1 in (2, 3)");
        cond("1 in (1)");
        cond("1 between (1, 2)");
        cond("1 between (1, 1)");
        cond("1 between (2, 3)");
        cond("1 between (0, 1)");
        cond("level > 10", map);
        cond("level > vipLevel", map);
        cond("level > vipLevel + 8", map);
        cond("level > vipLevel + 9", map);

        separator("逻辑");
        cond("1 && 1");
        cond("1 && 0");
        cond("0 && 1");
        cond("0 && 0");
        cond("1 and 1");
        cond("1 and 0");
        cond("0 and 1");
        cond("0 and 0");
        cond("1 || 1");
        cond("1 || 0");
        cond("0 || 1");
        cond("0 || 0");
        cond("1 or 1");
        cond("1 or 0");
        cond("0 or 1");
        cond("0 or 0");
        cond("not 1");
        cond("not 0");
        cond("not 999");
        cond("not 0 and 1");
        cond("!0 and 1");
        cond("!0 && 1 or 0");
        cond("0 && 1 or 0");

        separator("提示");
        tips("level > 20", map);
        tips("level >= 20", map);
        tips("level in (20, 30, 40)", map);
        tips("level between (20, 30)", map);
    }

    private static void separator(String desc) {
        System.out.println();
        System.out.println("--------------------------------");
        System.out.println(desc);
    }

    private static void calc(String exprString) {
        ExprNode expr = new ExprParser(new ExprLexer(exprString), ExampleExprConfig.config).parse();
        System.out.println(String.format("%s = %d", exprString, (long) expr.eval()));
    }

    private static void calc(String exprString, Map<String, Object> map) {
        ExprNode expr = new ExprParser(new ExprLexer(exprString), ExampleExprConfig.config).parse();
        System.out.println(String.format("%s = %d", exprString, (long) expr.eval(map)));
    }

    private static void cond(String exprString) {
        ExprNode expr = new ExprParser(new ExprLexer(exprString), ExampleExprConfig.config).parse();
        System.out.println(String.format("%s: %s", exprString, ExprUtil.isTrue(expr)));
    }

    private static void cond(String exprString, Map<String, Object> map) {
        ExprNode expr = new ExprParser(new ExprLexer(exprString), ExampleExprConfig.config).parse();
        System.out.println(String.format("%s: %s", exprString, ExprUtil.isTrue(expr, map)));
    }

    private static void tips(String exprString, Map<String, Object> map) {
        ExprNode expr = new ExprParser(new ExprLexer(exprString), ExampleExprConfig.config).parse();
        ExprContext ctx = new ExprContext(ExampleExprConfig.config);
        expr.eval(map, ctx);
        System.out.println(String.format("%s: %s", exprString, ExprUtil.makeSimpleTipsString(ctx)));
    }
}
