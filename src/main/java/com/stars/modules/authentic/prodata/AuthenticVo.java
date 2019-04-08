package com.stars.modules.authentic.prodata;

/**
 * Created by chenkeyu on 2016/12/22.
 */
public class AuthenticVo {
    private String levelsection;//等级区间[min+max]
    private int type;           //鉴宝类型1：元宝鉴宝，3：金币鉴宝
    private String reqitem;     //鉴宝需要消耗的物品(itemId+count)
    private int commondrop;     //普通掉落
    private int ensuredrop;     //保底掉落
    private int freetime;       //免费时间
    private int freecount;      //免费次数
    private int limitcount;     //限制次数

    public AuthenticVo(){}

    //内存数据
    private int minLevel;
    private int maxLevel;
    private int itemId;
    private int count;

    public String getLevelsection() {
        return levelsection;
    }

    public void setLevelsection(String levelsection) {
        this.levelsection = levelsection;
        String[] args = levelsection.split("\\+");
        try {
            this.minLevel = Integer.parseInt(args[0]);
            this.maxLevel = Integer.parseInt(args[1]);
        } catch (Exception e) {
            throw new IllegalArgumentException("" + e.getMessage());
        }
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getReqitem() {
        return reqitem;
    }

    public void setReqitem(String reqitem) {
        this.reqitem = reqitem;
        String[] args = reqitem.split("\\+");
        try {
            this.itemId = Integer.parseInt(args[0]);
            this.count = Integer.parseInt(args[1]);
        } catch (Exception e) {
            throw new IllegalArgumentException("" + e.getMessage());
        }
    }

    public int getCommondrop() {
        return commondrop;
    }

    public void setCommondrop(int commondrop) {
        this.commondrop = commondrop;
    }

    public int getEnsuredrop() {
        return ensuredrop;
    }

    public void setEnsuredrop(int ensuredrop) {
        this.ensuredrop = ensuredrop;
    }

    public int getFreetime() {
        return freetime;
    }

    public void setFreetime(int freetime) {
        this.freetime = freetime;
    }

    public int getFreecount() {
        return freecount;
    }

    public void setFreecount(int freecount) {
        this.freecount = freecount;
    }

    public int getLimitcount() {
        return limitcount;
    }

    public void setLimitcount(int limitcount) {
        this.limitcount = limitcount;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getItemId() {
        return itemId;
    }

    public int getCount() {
        return count;
    }
}
