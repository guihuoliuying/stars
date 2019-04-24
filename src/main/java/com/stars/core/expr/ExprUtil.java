package com.stars.core.expr;

import com.stars.core.expr.node.ExprNode;

/**
 * Created by zhaowenshuo on 2017/6/21.
 */
public class ExprUtil {

    public static boolean isTrue(ExprNode expr) {
        return (Long) expr.eval() != 0L;
    }

    public static void main(String[] args) {
//        System.out.println(makeSimpleTips(new ExprParser(new ExprLexer("1 + 1")).parse()));

        System.out.println(new ExprParser(new ExprLexer("1 + 1")).parse().eval(null, null));
        System.out.println(new ExprParser(new ExprLexer("1 + 1 * 2 ^ 3")).parse().eval(null, null));
        System.out.println(new ExprParser(new ExprLexer("((1+1)*(1+2))^3")).parse().eval(null, null));
        System.out.println(new ExprParser(new ExprLexer("(1+1)*(1+2)^3")).parse().eval(null, null));

//        new ExprParser(new ExprLexer("level > 1 and level < 10")).parse();
//        new ExprParser(new ExprLexer("(level > 1]")).parse();

        System.out.println(new ExprParser(new ExprLexer("1 + 1 * 2 ^ 3")).parse().inorderString());
        System.out.println(new ExprParser(new ExprLexer("((1+1)*(1+2))^3")).parse().inorderString());
        System.out.println(new ExprParser(new ExprLexer("(1+1)*(1+2)^3")).parse().inorderString());
        System.out.println(new ExprParser(new ExprLexer("level in (1,2,3,4,5,6)")).parse().inorderString());
        System.out.println(new ExprParser(new ExprLexer("{add, 1, 2}")).parse().inorderString());

        // relation
        System.out.println(new ExprParser(new ExprLexer("1 > 1")).parse().inorderString());
        // logic
        System.out.println(new ExprParser(new ExprLexer("1 and 1")).parse().inorderString());

        //
        ExprContext ctx = new ExprContext();
        new ExprParser(new ExprLexer("1 == 1 and 1 != 1")).parse().eval(null, ctx);
        System.out.println(ctx.getFalseStack());

        //
        System.out.println(ExprUtil.isTrue(new ExprParser(new ExprLexer("1 + 1 == 2")).parse()));
    }

}
