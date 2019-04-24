package com.stars.core.expr;

import com.stars.core.expr.node.ExprNode;
import com.stars.core.expr.node.basic.ExprDigitsNode;
import com.stars.core.expr.node.basic.ExprStringNode;
import com.stars.core.expr.node.dataset.ExprDataSetNode;
import com.stars.core.expr.node.dataset.ExprDataSetWhereNode;
import com.stars.core.expr.node.dataset.where.ExprDataSetWhereBetweenNode;
import com.stars.core.expr.node.dataset.where.ExprDataSetWhereInNode;
import com.stars.core.expr.node.dataset.where.ExprDataSetWhereRelationNode;
import com.stars.core.expr.node.func.ExprFuncNode;
import com.stars.core.expr.node.oparith.*;
import com.stars.core.expr.node.oplogic.ExprAndNode;
import com.stars.core.expr.node.oplogic.ExprNotNode;
import com.stars.core.expr.node.oplogic.ExprOrNode;
import com.stars.core.expr.node.oprelation.*;
import com.stars.core.expr.node.value.ExprValueNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.stars.core.expr.ExprTag.*;

/**
 * Created by zhaowenshuo on 2017/3/24.
 */
public class ExprParser {

    private ExprLexer lexer;
    private ExprToken lookahead;
    private Queue<ExprToken> parsedTokenQueue;
    private ExprConfig config;

    public ExprParser(ExprLexer lexer) {
        this.lexer = lexer;
        this.parsedTokenQueue = new LinkedList<>();
        this.lookahead = lexer.scan();
    }

    public ExprParser(ExprLexer lexer, ExprConfig config) {
        this(lexer);
        this.config = config;
    }

    private ExprToken match(int tag) {
        if (lookahead != null && lookahead.tag() == tag) {
            ExprToken ret = lookahead;
            lookahead = lexer.scan();
            if (lookahead == null) {
                lookahead = new ExprToken(TAG_EOF, Integer.MAX_VALUE);
            }
            parsedTokenQueue.offer(ret);
            return ret;
        } else {
            throw new IllegalStateException("条件解析异常" +
                    "|expected:" + ExprTag.toString(tag) + "|actual:" + lookahead +
                    "|已解析:" + parsedTokenQueue);
        }
    }

    public ExprNode parse() {
        ExprNode ret = parseOrExpr();
        if (lookahead.tag() != TAG_EOF) {
            throw new IllegalStateException("push condition parser error");
        }
        return ret;
    }



    // or
    private ExprNode parseOrExpr() {
        ExprNode left = parseAndExpr();
        ExprNode ret = parseOrExpr0(left);
        if (ret != null) {
            return ret;
        } else {
            return left;
        }
    }

    private ExprNode parseOrExpr0(ExprNode left) {
        ExprNode ret = null;
        if (lookahead.tag() == TAG_OR) {
            match(TAG_OR);
            ret = new ExprOrNode(config, left, parseAndExpr());
            ret = parseOrExpr0(ret);
        }
        if (ret != null) {
            return ret;
        } else {
            return left;
        }
    }

    private ExprNode parseAndExpr() {
//        ExprNode ret = null;
        ExprNode left = parseNotExpr();
        ExprNode ret = parseAndExpr0(left);
        if (ret != null) {
            return ret;
        } else {
            return left;
        }
    }

    private ExprNode parseAndExpr0(ExprNode left) {
        ExprNode ret = null;
        if (lookahead.tag() == TAG_AND) {
            match(TAG_AND);
            ret = new ExprAndNode(config, left, parseNotExpr());
            ret = parseAndExpr0(ret);
        }
        if (ret != null) {
            return ret;
        } else {
            return left;
        }
    }

    private ExprNode parseNotExpr() {
        if (lookahead.tag() == TAG_NOT) {
            match(TAG_NOT);
            return new ExprNotNode(config, parseNotExpr());
        } else {
            return parseRelationExpr();
        }
    }

    private ExprNode parseRelationExpr() {
//        ExprNode left = parseNumberExpr();
        ExprNode left = parseAddSubExpr();
        return parseRelationExpr0(left);
    }

    private ExprNode parseRelationExpr0(ExprNode left) {
        ExprNode ret = null;
        switch (lookahead.tag()) {
            case TAG_RELATION_OP:
                String op = match(TAG_RELATION_OP).lexeme();
//                ExprNode right = parseNumberExpr();
                ExprNode right = parseAddSubExpr();
                switch (op) {
                    case "==":
                        ret = new ExprEqNode(config, left, right);
                        break;
                    case "!=":
                        ret = new ExprNeNode(config, left, right);
                        break;
                    case ">":
                        ret = new ExprGtNode(config, left, right);
                        break;
                    case ">=":
                        ret = new ExprGeNode(config, left, right);
                        break;
                    case "<":
                        ret = new ExprLtNode(config, left, right);
                        break;
                    case "<=":
                        ret = new ExprLeNode(config, left, right);
                        break;
                }
                break;
            case TAG_BETWEEN:
                match(TAG_BETWEEN);
                match('(');
//                ExprNode rln = parseNumberExpr();
                ExprNode rln = parseAddSubExpr();
                match(',');
//                ExprNode rrn = parseNumberExpr();
                ExprNode rrn = parseAddSubExpr();
                match(')');
                ret = new ExprBetweenNode(config, left, rln, rrn);
                break;
            case TAG_IN:
                List<ExprNode> el = new ArrayList<>();
                match(TAG_IN);
                match('(');
//                el.add(parseNumberExpr());
                el.add(parseAddSubExpr());
                parseRelationEnumExpr(el);
                match(')');
                ret = new ExprInNode(config, left, el);
                break;
        }
        if (ret != null) {
            return ret;
        } else {
            return left;
        }
    }

    // + -
    private ExprNode parseAddSubExpr() {
        ExprNode left = parseMulDivModExpr();
        ExprNode ret = parseAddSubExpr0(left);
        if (ret != null) {
            return ret;
        } else {
            return left;
        }
    }

    private ExprNode parseAddSubExpr0(ExprNode left) {
        ExprNode ret = null;
        if (lookahead.tag() == TAG_OP_ADD) {
            match(TAG_OP_ADD);
            ret = new ExprAddNode(null, left, parseMulDivModExpr());
            ret = parseAddSubExpr0(ret);
        } else if (lookahead.tag() == TAG_OP_SUB) {
            match(TAG_OP_SUB);
            ret = new ExprSubNode(null, left, parseMulDivModExpr());
            ret = parseAddSubExpr0(ret);
        }
        if (ret != null) {
            return ret;
        } else {
            return left;
        }
    }

    // * / %
    private ExprNode parseMulDivModExpr() {
        ExprNode left = parsePowExpr();
        ExprNode ret = parseMulDivModExpr0(left);
        if (ret != null) {
            return ret;
        } else {
            return left;
        }
    }

    private ExprNode parseMulDivModExpr0(ExprNode left) {
        ExprNode ret = null;
        if (lookahead.tag() == TAG_OP_MUL) {
            match(TAG_OP_MUL);
            ret = new ExprMulNode(null, left, parsePowExpr());
            ret = parseMulDivModExpr0(ret);
        } else if (lookahead.tag() == TAG_OP_SUB) {
            match(TAG_OP_DIV);
            ret = new ExprDivNode(null, left, parsePowExpr());
            ret = parseMulDivModExpr0(ret);
        } else if (lookahead.tag() == TAG_OP_SUB) {
            match(TAG_OP_MOD);
            ret = new ExprModNode(null, left, parsePowExpr());
            ret = parseMulDivModExpr0(ret);
        }
        if (ret != null) {
            return ret;
        } else {
            return left;
        }
    }


    // ^
    private ExprNode parsePowExpr() {
        ExprNode left = parseNumberExpr();
        ExprNode ret = parsePowExpr0(left);
        if (ret != null) {
            return ret;
        } else {
            return left;
        }
    }

    private ExprNode parsePowExpr0(ExprNode left) {
        ExprNode ret = null;
        if (lookahead.tag() == TAG_OP_POW) {
            match(TAG_OP_POW);
            ret = new ExprPowNode(null, left, parseNumberExpr());
            ret = parsePowExpr0(ret);
        }
        if (ret != null) {
            return ret;
        } else {
            return left;
        }
    }

    private ExprNode parseNumberExpr() {
        ExprNode ret = null;
        switch (lookahead.tag()) {
            case TAG_DIGITS:
                ret = new ExprDigitsNode(config, lookahead.lexeme());
                match(TAG_DIGITS);
                break;
            case TAG_IDENTIFIER:
                ret = new ExprValueNode(config, lookahead.lexeme());
                match(TAG_IDENTIFIER);
                break;
            case TAG_BRACKET_LEFT:
                match('[');
                String dataSetName = match(TAG_IDENTIFIER).lexeme();
                List<ExprDataSetWhereNode> whereList = new ArrayList<>();
                parseDataSetCond(whereList); // fixme:
                match(']');
                ret = new ExprDataSetNode(config, dataSetName, whereList);
                break;
            case TAG_PARENTHESIS_LEFT:
                match('(');
                ret = parseOrExpr();
                match(')');
                break;
            case TAG_BRACE_LEFT:
                match('{');
                String funcName = match(TAG_IDENTIFIER).lexeme();
                List<ExprNode> paramList = new ArrayList<>();
                parseFuncParams(paramList);
                match('}');
                ret = new ExprFuncNode(config, funcName, paramList);
                break;
        }
        if (ret != null) {
            return ret;
        } else {
            throw new IllegalStateException("parseNumberExpr");
        }
    }

    private void parseDataSetCond(List<ExprDataSetWhereNode> whereList) {
        if (lookahead.tag() == ',') {
            match(',');
            String fieldName = match(TAG_IDENTIFIER).lexeme();
            parseDataSetCond0(whereList, fieldName);
        }
    }

    private void parseDataSetCond0(List<ExprDataSetWhereNode> whereList, String fieldName) {
        switch (lookahead.tag()) {
            case TAG_RELATION_OP:
                String op = match(TAG_RELATION_OP).lexeme();
//                whereList.add(new ExprDataSetWhereRelationNode(fieldName, parseNumberExpr(), op));
                whereList.add(new ExprDataSetWhereRelationNode(fieldName, parseAddSubExpr(), op));
                parseDataSetCond(whereList);
                break;
            case TAG_BETWEEN:
                match(TAG_BETWEEN);
                match('(');
//                ExprNode rl = parseNumberExpr();
                ExprNode rl = parseAddSubExpr();
                match(',');
//                ExprNode rr = parseNumberExpr();
                ExprNode rr = parseAddSubExpr();
                match(')');
                whereList.add(new ExprDataSetWhereBetweenNode(fieldName, rl, rr));
                parseDataSetCond(whereList);
                break;
            case TAG_IN:
                match(TAG_IN);
                match('(');
                List<ExprNode> el = new ArrayList<>();
//                el.add(parseNumberExpr());
                el.add(parseAddSubExpr());
                parseRelationEnumExpr(el); // fixme:
                match(')');
                whereList.add(new ExprDataSetWhereInNode(fieldName, el));
                parseDataSetCond(whereList);
                break;
        }
    }

    private void parseRelationEnumExpr(List<ExprNode> el) {
        if (lookahead.tag() == ',') {
            match(',');
//            el.add(parseNumberExpr());
            el.add(parseAddSubExpr());
            parseRelationEnumExpr(el);
        }
    }

    private void parseFuncParams(List<ExprNode> paramList) {
        if (lookahead.tag() == ',') {
            match(',');
            paramList.add(parseFuncParam());
            parseFuncParams(paramList);
        }
    }

    private ExprNode parseFuncParam() {
        if (lookahead.tag() == TAG_STRING) {
            String str = match(TAG_STRING).lexeme();
            return new ExprStringNode(config, str);
        } else {
//            return parseNumberExpr();
            return parseAddSubExpr();
        }
    }
}
