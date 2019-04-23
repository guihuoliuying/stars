package com.stars.core.expr;

import com.stars.core.expr.node.ExprNode;
import com.stars.core.expr.node.basic.ExprDigitsNode;
import com.stars.core.expr.node.oprelation.PcnRelation;
import com.stars.core.expr.node.value.ExprValueNode;
import com.stars.core.module.Module;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/6/21.
 */
public class ExprUtil {

    public static boolean isTrue(ExprNode expr, Map<String, Module> moduleMap) {
        return (Long) expr.eval(moduleMap) != 0L;
    }

    public static String makeSimpleTips(ExprNode expr) {
        if (expr instanceof PcnRelation) {
            ExprNode left = ((PcnRelation) expr).left();
            ExprNode right = ((PcnRelation) expr).right();
            if ((left instanceof ExprValueNode || left instanceof ExprDigitsNode)
                    && (right instanceof ExprValueNode || right instanceof ExprDigitsNode)) {
                String op = "大于";
                switch (((PcnRelation) expr).operator()) {
                    case ">": op = "大于"; break;
                    case ">=": op = "大于等于"; break;
                    case "<": op = "小于"; break;
                    case "<=": op = "小于等于"; break;
                    case "==": op = "等于"; break;
                    case "!=": op = "不等于"; break;
                }
                return "需要" + left.toString() + op + right.toString();
            }
        }
        return null;
    }

    public static void main(String[] args) {
//        System.out.println(makeSimpleTips(new ExprParser(new ExprLexer("1 + 1")).parse()));

        System.out.println(new ExprParser(new ExprLexer("1 + 1")).parse().eval(null));
        System.out.println(new ExprParser(new ExprLexer("1 + 1 * 2 ^ 3")).parse().eval(null));
        System.out.println(new ExprParser(new ExprLexer("((1+1)*(1+2))^3")).parse().eval(null));
        System.out.println(new ExprParser(new ExprLexer("(1+1)*(1+2)^3")).parse().eval(null));

//        new ExprParser(new ExprLexer("level > 1 and level < 10")).parse();
//        new ExprParser(new ExprLexer("(level > 1]")).parse();

        System.out.println(new ExprParser(new ExprLexer("1 + 1 * 2 ^ 3")).parse().inorderString());
        System.out.println(new ExprParser(new ExprLexer("((1+1)*(1+2))^3")).parse().inorderString());
        System.out.println(new ExprParser(new ExprLexer("(1+1)*(1+2)^3")).parse().inorderString());
        System.out.println(new ExprParser(new ExprLexer("level in (1,2,3,4,5,6)")).parse().inorderString());
        System.out.println(new ExprParser(new ExprLexer("{add, 1, 2}")).parse().inorderString());
    }

}
