package com.stars.modules.trump.prodata;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.modules.trump.userdata.RoleTrumpRow;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/10/18.
 */
public class TrumpKarmaVo {
    private int id;
    private String name;
    private String active;// '激活条件，格式为：trupid+level,trupid+level……',
    private String attribute;//'仙缘属性，格式为：attributename=数值1，attributebutename=数值2. 此处配置的属性仅代表当前等级增加的属性加成',
    private int order;//排序,大的排前面
    private Attribute attr = new Attribute();
    private Map<Integer, Integer> activeCondition = new HashMap<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
        activeCondition = StringUtil.toMap(active, Integer.class, Integer.class, '+', ',');
    }


    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
        attr = new Attribute(attribute);
    }

    public Attribute getAttr() {
        return attr;
    }

    public boolean canActive(Map<Integer, RoleTrumpRow> roleTrumpMap) {
        for (Map.Entry<Integer, Integer> entry : activeCondition.entrySet()) {
            Integer trumpId = entry.getKey();
            Integer level = entry.getValue();
            RoleTrumpRow roleTrumpRow = roleTrumpMap.get(trumpId);
            if (roleTrumpRow != null) {
                if (roleTrumpRow.getLevel()+1 < level) {
                    return false;
                }
            } else {
                return false;
            }

        }
        return true;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void writeBuff(NewByteBuffer buff) {
        buff.writeString(name);
        buff.writeString(active);// '激活条件，格式为：trupid+level,trupid+level……',
        buff.writeString(attribute);//'仙缘属性，格式为：attributename=数值1，attributebutename=数值2. 此处配置的属性仅代表当前等级增加的属性加成',
        buff.writeString("");//效果
        buff.writeInt(FormularUtils.calFightScore(attr));//战力
    }
}
