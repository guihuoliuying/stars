package com.stars.modules.marry.prodata;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.util.StringUtil;

/**
 * Created by zhoujin on 2017/4/14.
 */
public class MarryRingLvl {
    private int ringid;
    private short level;
    private String attr;
    private int fightPower;// 可增加战力
    private Attribute attribute = new Attribute();//戒指属性

    public int getRingid() {return this.ringid;}

    public void setRingid(int ringid) { this.ringid = ringid;}

    public short getLevel() {return this.level;}

    public void setLevel(short level) {this.level = level;}

    public String getAttr() {return this.attr;}

    public void setAttr(String attr) {
        this.attr = attr;
        if (StringUtil.isEmpty(attr) || "0".equals(attr)) {
            return;
        }
        attribute = new Attribute(attr);
        this.fightPower = FormularUtils.calFightScore(attribute);
    }

    public int getFightPower() {return this.fightPower;}

    public Attribute getAttribute() {return this.attribute;}
}
