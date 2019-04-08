package com.stars.modules.marry.prodata;

/**
 * Created by zhouyaohui on 2016/12/13.
 */
public class MarryActivityVo {
    private int activitytype;  // 1=发红包活动， 2=放烟花活动，3=发喜糖活动
    private int times;  // 次数
    private String cost;    // 活动花费
    private String reward;  // 活动奖励

    public int getActivitytype() {
        return activitytype;
    }

    public void setActivitytype(int activitytype) {
        this.activitytype = activitytype;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }

}
