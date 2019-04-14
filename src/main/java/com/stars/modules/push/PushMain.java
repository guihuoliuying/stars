package com.stars.modules.push;

import com.stars.core.expr.ExprLexer;
import com.stars.core.expr.ExprParser;
import com.stars.core.expr.node.ExprNode;

/**
 * Created by zhaowenshuo on 2017/3/23.
 */
public class PushMain {

    public static void main(String[] args) {
//        System.out.println("(:" + String.format("0x%04X", (int) '(')); // 40
//        System.out.println("):" + String.format("0x%04X", (int) ')')); // 41
//        System.out.println("[:" + String.format("0x%04X", (int) '[')); // 91
//        System.out.println("]:" + String.format("0x%04X", (int) ']')); // 93
//        System.out.println("{:" + String.format("0x%04X", (int) '{')); // 123
//        System.out.println("}:" + String.format("0x%04X", (int) '}')); // 125
//        System.out.println("<:" + String.format("0x%04X", (int) '<')); // 60
//        System.out.println(">:" + String.format("0x%04X", (int) '>')); // 62
//        System.out.println("=:" + String.format("0x%04X", (int) '=')); // 61
//        System.out.println("!:" + String.format("0x%04X", (int) '!')); // 33
//        System.out.println(",:" + String.format("0x%04X", (int) ',')); // 44
//
//        System.out.println("0:" + String.format("0x%04X", (int) '0')); // 0x30
//        System.out.println("9:" + String.format("0x%04X", (int) '9')); // 0x39
//        System.out.println("a:" + String.format("0x%04X", (int) 'a')); // 0x39
//        System.out.println("z:" + String.format("0x%04X", (int) 'z')); // 0x39
//        System.out.println("A:" + String.format("0x%04X", (int) 'A')); // 0x39
//        System.out.println("Z:" + String.format("0x%04X", (int) 'Z')); // 0x39

        ExprLexer lexer = new ExprLexer(
                "1 == 1 and 1 != 0 or 2 == 1 and (1 == 2)");
        // level >= 1
        // level between (1, 20)
        // level in (1, 2, 3)
        // [bag, lv > 10, quality > 10]
        // {add, 1, 2}

//        ExprToken token = null;
//        while ((token = lexer.scan()) != null) {
//            System.out.println(token);
//        }
        ExprParser parser = new ExprParser(lexer);
        ExprNode ret = parser.parse();
        System.out.println("ret: " + ret.eval(null));
    }

}
