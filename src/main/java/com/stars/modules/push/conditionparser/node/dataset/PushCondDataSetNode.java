package com.stars.modules.push.conditionparser.node.dataset;

import com.stars.core.module.Module;
import com.stars.modules.push.conditionparser.PushCondGlobal;
import com.stars.modules.push.conditionparser.node.PushCondNode;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/22.
 */
public class PushCondDataSetNode extends PushCondNode {

    private String name;
    private List<PushCondDataSetWhereNode> whereList;

    public PushCondDataSetNode(String name, List<PushCondDataSetWhereNode> whereList) {
        this.name = name;
        this.whereList = whereList;
        Set<String> fieldSet = PushCondGlobal.getFieldSet(name);
        for (PushCondDataSetWhereNode whereNode : whereList) {
            if (!fieldSet.contains(whereNode.getFieldName())) {
                throw new IllegalStateException("条件解析异常|不存在维度:" + whereNode.getFieldName() + "|集合:" + name);
            }
        }
    }

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        PushCondDataSet dataSet = PushCondGlobal.newDataSet(name, moduleMap);
        long c = 0L;
        while (dataSet.hasNext()) {
            PushCondData data = dataSet.next();
            if (!data.isInvalid()) { // 是否有效
                boolean flag = true;
                for (PushCondDataSetWhereNode whereNode : whereList) {
                    if (!whereNode.eval(data, moduleMap)) {
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
