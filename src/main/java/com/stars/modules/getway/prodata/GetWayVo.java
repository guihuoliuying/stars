package com.stars.modules.getway.prodata;

import com.stars.modules.push.conditionparser.PushCondLexer;
import com.stars.modules.push.conditionparser.PushCondParser;
import com.stars.modules.push.conditionparser.node.PushCondNode;

/**
 * Created by zhaowenshuo on 2017/6/22.
 */
public class GetWayVo {

    private int getWayId;
    private String condition;
    private PushCondNode condExpr;

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
        this.condExpr = new PushCondParser(new PushCondLexer(condition)).parse();
    }

    public PushCondNode getCondExpr() {
        return this.condExpr;
    }
}
