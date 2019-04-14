package com.stars.modules.getway.prodata;

import com.stars.core.expr.ExprLexer;
import com.stars.core.expr.ExprParser;
import com.stars.core.expr.node.ExprNode;

/**
 * Created by zhaowenshuo on 2017/6/22.
 */
public class GetWayVo {

    private int getWayId;
    private String condition;
    private ExprNode condExpr;

    public int getGetWayId() {
        return getWayId;
    }

    public void setGetWayId(int getWayId) {
        this.getWayId = getWayId;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
        this.condExpr = new ExprParser(new ExprLexer(condition)).parse();
    }

    public ExprNode getCondExpr() {
        return this.condExpr;
    }
}
