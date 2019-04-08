package com.stars.modules.refine.prodata;

import com.stars.modules.push.conditionparser.PushCondLexer;
import com.stars.modules.push.conditionparser.PushCondParser;
import com.stars.modules.push.conditionparser.node.PushCondNode;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-08-02.
 */
public class RefineVo {
    private int itemId;
    private String output;
    private String condition;
    private int order;

    private Map<Integer, Integer> outputItemMap = new HashMap<>();

    private PushCondNode condChecker;

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
        this.outputItemMap = StringUtil.toMap(output, Integer.class, Integer.class, '+', ',');
    }

    public Map<Integer, Integer> getOutputItemMap() {
        return outputItemMap;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
        this.condChecker = new PushCondParser(new PushCondLexer(condition)).parse();
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public PushCondNode getCondChecker() {
        return condChecker;
    }

    @Override
    public String toString() {
        return "RefineVo{" +
                "itemId=" + itemId +
                ", output='" + output + '\'' +
                ", condition='" + condition + '\'' +
                ", order=" + order +
                '}';
    }
}
