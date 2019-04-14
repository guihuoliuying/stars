package com.stars.core.expr.node.dataset;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.node.ExprNode;

import java.util.List;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/22.
 */
public class ExprDataSetNode extends ExprNode {

    private String name;
    private List<ExprDataSetWhereNode> whereList;

    public ExprDataSetNode(ExprConfig config, String name, List<ExprDataSetWhereNode> whereList) {
        super(config);
        this.name = name;
        this.whereList = whereList;
        Set<String> fieldSet = config.getFieldSet(name);
        for (ExprDataSetWhereNode whereNode : whereList) {
            if (!fieldSet.contains(whereNode.getFieldName())) {
                throw new IllegalStateException("条件解析异常|不存在维度:" + whereNode.getFieldName() + "|集合:" + name);
            }
        }
    }

    @Override
    public Object eval(Object obj) {
        ExprDataSet dataSet = config.newDataSet(name, obj);
        long c = 0L;
        while (dataSet.hasNext()) {
            ExprData data = dataSet.next();
            if (!data.isInvalid()) { // 是否有效
                boolean flag = true;
                for (ExprDataSetWhereNode whereNode : whereList) {
                    if (!whereNode.eval(data, obj)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    if (!data.isOverlay())
                        c++;
                    else
                        c += data.getOverlayCount(); // 解决有叠加数量的问题
                }
            }
        }
        return c;
    }

}
