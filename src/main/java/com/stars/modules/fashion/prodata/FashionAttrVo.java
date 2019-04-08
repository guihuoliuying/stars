package com.stars.modules.fashion.prodata;

import com.stars.core.attr.Attribute;

/**
 * Created by zhanghaizhen on 2017/5/11.
 */
public class FashionAttrVo {
    private int fashionId;
    private byte timeType;
    private String attr;
    Attribute attribute = new Attribute();

    public int getFashionId() {
        return fashionId;
    }

    public void setFashionId(int fashionId) {
        this.fashionId = fashionId;
    }

    public byte getTimeType() {
        return timeType;
    }

    public void setTimeType(byte timeType) {
        this.timeType = timeType;
    }

    public String getAttr() {
        return attr;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttr(String attr) {
        this.attr = attr;
        attribute.strToAttribute(attr);

    }
}
