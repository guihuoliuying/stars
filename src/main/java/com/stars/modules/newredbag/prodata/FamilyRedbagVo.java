package com.stars.modules.newredbag.prodata;

/**
 * Created by zhouyaohui on 2017/2/14.
 */
public class FamilyRedbagVo {
    private int redId;
    private String describe;
    private int type;
    private String value;
    private String sendCount;
    private int rank;

    private int minCount;
    private int maxCount;
    private int itemId;
    private int itemValue;

    public int getRedId() {
        return redId;
    }

    public void setRedId(int redId) {
        this.redId = redId;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        String[] values = value.split("[+]");
        itemId = Integer.valueOf(values[0]);
        itemValue = Integer.valueOf(values[1]);
    }

    public String getSendCount() {
        return sendCount;
    }

    public void setSendCount(String sendCount) {
        this.sendCount = sendCount;
        String[] counts = sendCount.split("[+]");
        minCount = Integer.valueOf(counts[0]);
        maxCount = Integer.valueOf(counts[1]);
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getMinCount() {
        return minCount;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public int getItemId() {
        return itemId;
    }

    public int getItemValue() {
        return itemValue;
    }
}
