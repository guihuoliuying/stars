package com.stars.modules.newequipment.prodata;

/**
 * Created by wuyuxing on 2016/11/9.
 */
public class ExtAttrWeightVo {
    private String attrName;
    private int value;
    private int weight;

    public ExtAttrWeightVo() {
    }

    public ExtAttrWeightVo(String strData) {
        String[] array = strData.split("\\+");
        this.attrName = array[0];
        this.value = Integer.parseInt(array[1]);
        this.weight = Integer.parseInt(array[2]);
    }

    public ExtAttrWeightVo(String attrName, int value, int weight) {
        this.attrName = attrName;
        this.value = value;
        this.weight = weight;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
