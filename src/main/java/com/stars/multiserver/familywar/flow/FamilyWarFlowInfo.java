package com.stars.multiserver.familywar.flow;

import com.stars.util.LogUtil;

public class FamilyWarFlowInfo {

    private int warType;//比赛类型
    private byte state;//流程状态
    private String time;//流程时间

    public FamilyWarFlowInfo(int warType, String time) {
        this.warType = warType;
        this.time = time;
        LogUtil.info("warType:{},time:{}", warType, time);
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getWarType() {
        return warType;
    }

    public void setWarType(int warType) {
        this.warType = warType;
    }

}
