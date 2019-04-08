package com.stars.modules.push.conditionparser;

import com.stars.core.module.Module;
import com.stars.modules.push.conditionparser.node.PushCondNode;
import com.stars.modules.push.conditionparser.node.basic.PcnDigits;
import com.stars.modules.push.conditionparser.node.oprelation.PcnRelation;
import com.stars.modules.push.conditionparser.node.value.PcnValue;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/6/21.
 */
public class CondUtils {

    public static boolean isTrue(PushCondNode expr, Map<String, Module> moduleMap) {
        return (Long) expr.eval(moduleMap) != 0L;
    }

    public static String makeSimpleTips(PushCondNode expr) {
        if (expr instanceof PcnRelation) {
            PushCondNode left = ((PcnRelation) expr).left();
            PushCondNode right = ((PcnRelation) expr).right();
            if ((left instanceof PcnValue || left instanceof PcnDigits)
                && (right instanceof PcnValue || right instanceof PcnDigits)) {
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
        System.out.println(makeSimpleTips(new PushCondParser(new PushCondLexer("level>1")).parse()));
    }

}
