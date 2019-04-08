package com.stars.modules.newequipment.prodata;

import com.stars.util.StringUtil;

import java.util.Map;

/**
 * 装备强化数据;
 * Created by wuyuxing on 2016/11/8.
 */
public class EquipStrengthVo {
    private int level;          //强化等级
    private byte type;          //装备类型
    private String material;    //由前一级升级到当前等级需要的材料, 格式为:itemid+数量, itemid+数量
    private Map<Integer,Integer> materialMap;
    private int levelLimit;     //强化到当前等级需要的角色等级
    private int attrPencent;    //基础属性加成百分比. 百分数
    private int attrAdd;        //基础属性加成绝对值(固定加成)

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) throws Exception {
        this.material = material;
        this.materialMap = StringUtil.toMap(material, Integer.class, Integer.class, '+', ',');
    }

    public int getLevelLimit() {
        return levelLimit;
    }

    public void setLevelLimit(int levelLimit) {
        this.levelLimit = levelLimit;
    }

    public int getAttrPencent() {
        return attrPencent;
    }

    public void setAttrPencent(int attrPencent) {
        this.attrPencent = attrPencent;
    }

    public Map<Integer, Integer> getMaterialMap() {
        return materialMap;
    }

    public int getAttrAdd() {
        return attrAdd;
    }

    public void setAttrAdd(int attrAdd) {
        this.attrAdd = attrAdd;
    }
}
