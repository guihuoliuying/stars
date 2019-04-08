package com.stars.modules.push.conditionparser;

import com.stars.modules.push.conditionparser.node.PushCondNode;
import com.stars.modules.push.conditionparser.node.basic.PcnDigits;
import com.stars.modules.push.conditionparser.node.basic.PcnString;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSetNode;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSetWhereNode;
import com.stars.modules.push.conditionparser.node.dataset.where.PcdswnBetween;
import com.stars.modules.push.conditionparser.node.dataset.where.PcdswnIn;
import com.stars.modules.push.conditionparser.node.dataset.where.PcdswnRelation;
import com.stars.modules.push.conditionparser.node.func.PcnFunc;
import com.stars.modules.push.conditionparser.node.oplogic.PcnAnd;
import com.stars.modules.push.conditionparser.node.oplogic.PcnNot;
import com.stars.modules.push.conditionparser.node.oplogic.PcnOr;
import com.stars.modules.push.conditionparser.node.oprelation.PcnBetween;
import com.stars.modules.push.conditionparser.node.oprelation.PcnIn;
import com.stars.modules.push.conditionparser.node.oprelation.PcnRelation;
import com.stars.modules.push.conditionparser.node.value.PcnValue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.stars.modules.push.conditionparser.PushCondParserTag.*;

/**
 * Created by zhaowenshuo on 2017/3/24.
 */
public class PushCondParser {

    private PushCondLexer lexer;
    private PushCondToken lookahead;

    private Queue<PushCondToken> parsedTokenQueue;

    public PushCondParser(PushCondLexer lexer) {
        this.lexer = lexer;
        this.parsedTokenQueue = new LinkedList<>();
        this.lookahead = lexer.scan();
    }

    private PushCondToken match(int tag) {
        if (lookahead != null && lookahead.tag() == tag) {
            PushCondToken ret = lookahead;
            lookahead = lexer.scan();
            if (lookahead == null) {
                lookahead = new PushCondToken(TAG_EOF, Integer.MAX_VALUE);
            }
            parsedTokenQueue.offer(ret);
            return ret;
        } else {
            throw new IllegalStateException("条件解析异常" +
                    "|expected:" + PushCondGlobal.toString(tag) + "|actual:" + lookahead +
                    "|已解析:" + parsedTokenQueue);
        }
    }

    public PushCondNode parse() {
        PushCondNode ret = parseOrExpr();
        if (lookahead.tag() != TAG_EOF) {
            throw new IllegalStateException("push condition parser error");
        }
        return ret;
    }

    private PushCondNode parseOrExpr() {
        PushCondNode left = parseAndExpr();
        PushCondNode ret = parseOrExpr0(left);
        if (ret != null) {
            return ret;
        } else {
            return left;
        }
    }

    private PushCondNode parseOrExpr0(PushCondNode left) {
        PushCondNode ret = null;
        if (lookahead.tag() == TAG_OR) {
            match(TAG_OR);
            ret = new PcnOr(left, parseAndExpr());
            ret = parseOrExpr0(ret);
        }
        if (ret != null) {
            return ret;
        } else {
            return left;
        }
    }

    private PushCondNode parseAndExpr() {
//        PushCondNode ret = null;
        PushCondNode left = parseNotExpr();
        PushCondNode ret = parseAndExpr0(left);
        if (ret != null) {
            return ret;
        } else {
            return left;
        }
    }

    private PushCondNode parseAndExpr0(PushCondNode left) {
        PushCondNode ret = null;
        if (lookahead.tag() == TAG_AND) {
            match(TAG_AND);
            ret = new PcnAnd(left, parseNotExpr());
            ret = parseAndExpr0(ret);
        }
        if (ret != null) {
            return ret;
        } else {
            return left;
        }
    }

    private PushCondNode parseNotExpr() {
        if (lookahead.tag() == TAG_NOT) {
            match(TAG_NOT);
            return new PcnNot(parseNotExpr());
        } else {
            return parseRelationExpr();
        }
    }

    private PushCondNode parseRelationExpr() {
        PushCondNode left = parseNumberExpr();
        return parseRelationExpr0(left);
    }

    private PushCondNode parseRelationExpr0(PushCondNode left) {
        PushCondNode ret = null;
        switch (lookahead.tag()) {
            case TAG_RELATION_OP:
                String op = match(TAG_RELATION_OP).lexeme();
                PushCondNode right = parseNumberExpr();
                ret = new PcnRelation(left, right, op);
                break;
            case TAG_BETWEEN:
                match(TAG_BETWEEN);
                match('(');
                PushCondNode rln = parseNumberExpr();
                match(',');
                PushCondNode rrn = parseNumberExpr();
                match(')');
                ret = new PcnBetween(left, rln, rrn);
                break;
            case TAG_IN:
                List<PushCondNode> el = new ArrayList<>();
                match(TAG_IN);
                match('(');
                el.add(parseNumberExpr());
                parseRelationEnumExpr(el);
                match(')');
                ret = new PcnIn(left, el);
                break;
        }
        if (ret != null) {
            return ret;
        } else {
            return left;
        }
    }

    private PushCondNode parseNumberExpr() {
        PushCondNode ret = null;
        switch (lookahead.tag()) {
            case TAG_DIGITS:
                ret = new PcnDigits(lookahead.lexeme());
                match(TAG_DIGITS);
                break;
            case TAG_IDENTIFIER:
                ret = new PcnValue(lookahead.lexeme());
                match(TAG_IDENTIFIER);
                break;
            case TAG_BRACKET_LEFT:
                match('[');
                String dataSetName = match(TAG_IDENTIFIER).lexeme();
                List<PushCondDataSetWhereNode> whereList = new ArrayList<>();
                parseDataSetCond(whereList); // fixme:
                match(']');
                ret = new PushCondDataSetNode(dataSetName, whereList);
                break;
            case TAG_PARENTHESIS_LEFT:
                match('(');
                ret = parseOrExpr();
                match(')');
                break;
            case TAG_BRACE_LEFT:
                match('{');
                String funcName = match(TAG_IDENTIFIER).lexeme();
                List<PushCondNode> paramList = new ArrayList<>();
                parseFuncParams(paramList);
                match('}');
                ret = new PcnFunc(funcName, paramList);
                break;
        }
        if (ret != null) {
            return ret;
        } else {
            throw new IllegalStateException("parseNumberExpr");
        }
    }

    private void parseDataSetCond(List<PushCondDataSetWhereNode> whereList) {
        if (lookahead.tag() == ',') {
            match(',');
            String fieldName = match(TAG_IDENTIFIER).lexeme();
            parseDataSetCond0(whereList, fieldName);
        }
    }

    private void parseDataSetCond0(List<PushCondDataSetWhereNode> whereList, String fieldName) {
        switch (lookahead.tag()) {
            case TAG_RELATION_OP:
                String op = match(TAG_RELATION_OP).lexeme();
                whereList.add(new PcdswnRelation(fieldName, parseNumberExpr(), op));
                parseDataSetCond(whereList);
                break;
            case TAG_BETWEEN:
                match(TAG_BETWEEN);
                match('(');
                PushCondNode rl = parseNumberExpr();
                match(',');
                PushCondNode rr = parseNumberExpr();
                match(')');
                whereList.add(new PcdswnBetween(fieldName, rl, rr));
                parseDataSetCond(whereList);
                break;
            case TAG_IN:
                match(TAG_IN);
                match('(');
                List<PushCondNode> el = new ArrayList<>();
                el.add(parseNumberExpr());
                parseRelationEnumExpr(el); // fixme:
                match(')');
                whereList.add(new PcdswnIn(fieldName, el));
                parseDataSetCond(whereList);
                break;
        }
    }

    private void parseRelationEnumExpr(List<PushCondNode> el) {
        if (lookahead.tag() == ',') {
            match(',');
            el.add(parseNumberExpr());
            parseRelationEnumExpr(el);
        }
    }

    private void parseFuncParams(List<PushCondNode> paramList) {
        if (lookahead.tag() == ',') {
            match(',');
            paramList.add(parseFuncParam());
            parseFuncParams(paramList);
        }
    }

    private PushCondNode parseFuncParam() {
        if (lookahead.tag() == TAG_STRING) {
            String str = match(TAG_STRING).lexeme();
            return new PcnString(str);
        } else {
            return parseNumberExpr();
        }
    }
}
