package com.stars.modules.push;

import com.stars.modules.push.conditionparser.PushCondLexer;
import com.stars.modules.push.conditionparser.PushCondParser;
import com.stars.modules.push.conditionparser.node.PushCondNode;

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

        PushCondLexer lexer = new PushCondLexer(
                "1 == 1 and 1 != 0 or 2 == 1 and (1 == 2)");
        // level >= 1
        // level between (1, 20)
        // level in (1, 2, 3)
        // [bag, lv > 10, quality > 10]
        // {add, 1, 2}

//        PushCondToken token = null;
//        while ((token = lexer.scan()) != null) {
//            System.out.println(token);
//        }
        PushCondParser parser = new PushCondParser(lexer);
        PushCondNode ret = parser.parse();
        System.out.println("ret: " + ret.eval(null));
    }

}
