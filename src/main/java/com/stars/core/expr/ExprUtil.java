package com.stars.core.expr;

import com.google.common.base.Preconditions;
import com.stars.core.expr.node.ExprNode;
import com.stars.core.expr.node.basic.ExprDigitsNode;
import com.stars.core.expr.node.dataset.ExprDataSetNode;
import com.stars.core.expr.node.func.ExprFuncNode;
import com.stars.core.expr.node.oprelation.*;
import com.stars.core.expr.node.value.ExprValueNode;
import com.stars.core.expr.tips.ExprTips;
import com.stars.core.expr.tips.ExprTipsType;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/6/21.
 */
public class ExprUtil {

    public static boolean isTrue(ExprNode expr) {
        return (Long) expr.eval() != 0L;
    }

    public static boolean isTrue(ExprNode expr, Object obj) {
        return (Long) expr.eval(obj) != 0L;
    }

    public static String makeSimpleTipsString(ExprContext context) {
        List<Pair<ExprTips, String[]>> tipsList = makeTips(context);
        Pair<ExprTips, String[]> pair = tipsList.get(0);
        return pair.getFirst().makeString(pair.getValue());
    }

    public static List<Pair<ExprTips, String[]>> makeTips(ExprContext context) {
        List<Pair<ExprTips, String[]>> tipsList = new ArrayList<>();
        if (context.getFalseStack().size() == 0) {
            throw new IllegalStateException("the condition is not false");
        }
        Set<ExprNode> nodeSet = context.getFalseStack().pop();
        if (nodeSet != null) {
            ExprConfig config = context.getConfig();
            nodeSet.forEach(node -> {
                ExprTips tips = config.getTips(getType(node), getName(node), getPredicate(node));
                if (tips == null) {
                    throw new IllegalArgumentException();
                }
                tipsList.add(new Pair<>(tips, getParams(node)));
            });
        }
        return tipsList;
    }

    private static ExprTipsType getType(ExprNode node) {
        if (node instanceof ExprEqNode
                || node instanceof ExprNeNode
                || node instanceof ExprGtNode
                || node instanceof ExprGeNode
                || node instanceof ExprLtNode
                || node instanceof ExprLeNode) {
            return getType(((ExprBinaryRelationNode) node).getLeft());
        }
        if (node instanceof ExprInNode) {
            return getType(((ExprInNode) node).getChild());
        }
        if (node instanceof ExprBetweenNode) {
            return getType(((ExprBetweenNode) node).getChild());
        }
        if (node instanceof ExprValueNode) {
            return ExprTipsType.VALUE;

        } else if (node instanceof ExprFuncNode) {
            return ExprTipsType.FUNCTION;

        } else if (node instanceof ExprDataSetNode) {
            return ExprTipsType.DATASET;
        }
        return null;
    }

    private static String getName(ExprNode node) {
        if (node instanceof ExprEqNode
                || node instanceof ExprNeNode
                || node instanceof ExprGtNode
                || node instanceof ExprGeNode
                || node instanceof ExprLtNode
                || node instanceof ExprLeNode) {
            return getName(((ExprBinaryRelationNode) node).getLeft());
        }
        if (node instanceof ExprInNode) {
            return getName(((ExprInNode) node).getChild());
        }
        if (node instanceof ExprBetweenNode) {
            return getName(((ExprBetweenNode) node).getChild());
        }
        if (node instanceof ExprValueNode) {
            return ((ExprValueNode) node).getName();
        }
        if (node instanceof ExprFuncNode) {
            return ((ExprFuncNode) node).getName();
        }
        if (node instanceof ExprDataSetNode) {
            return ((ExprDataSetNode) node).getName();
        }
        return null;
    }

    private static String getPredicate(ExprNode node) {
        if (node instanceof ExprEqNode) {
            return "==";
        }
        if (node instanceof ExprNeNode) {
            return "!=";
        }
        if (node instanceof ExprGtNode) {
            return ">";
        }
        if (node instanceof ExprGeNode) {
            return ">=";
        }
        if (node instanceof ExprLtNode) {
            return "<";
        }
        if (node instanceof ExprLeNode) {
            return "<=";
        }
        if (node instanceof ExprInNode) {
            return "in";
        }
        if (node instanceof ExprBetweenNode) {
            return "between";
        }
        return "";
    }

    private static String[] getParams(ExprNode node) {
        if (node instanceof ExprEqNode
                || node instanceof ExprNeNode
                || node instanceof ExprGtNode
                || node instanceof ExprGeNode
                || node instanceof ExprLtNode
                || node instanceof ExprLeNode) {
            ExprNode right = ((ExprBinaryRelationNode) node).getRight();
            Preconditions.checkState(right instanceof ExprDigitsNode);
            return new String[]{right.eval().toString()};
        }
        if (node instanceof ExprInNode) {
            List<ExprNode> elemList = ((ExprInNode) node).getElemList();
            boolean isAllDigit = elemList.stream().allMatch(elem -> elem instanceof ExprDigitsNode);
            Preconditions.checkState(isAllDigit);
            return elemList
                    .stream()
                    .map(elem -> elem.eval().toString())
                    .toArray(String[]::new);
        }
        if (node instanceof ExprBetweenNode) {
            ExprNode rangeLeft = ((ExprBetweenNode) node).getRangeLeft();
            ExprNode rangeRight = ((ExprBetweenNode) node).getRangeRight();
            Preconditions.checkState(rangeLeft instanceof ExprDigitsNode);
            Preconditions.checkState(rangeRight instanceof ExprDigitsNode);
            return new String[]{rangeLeft.eval().toString(), rangeRight.eval().toString()};
        }
        return null;
    }

}
