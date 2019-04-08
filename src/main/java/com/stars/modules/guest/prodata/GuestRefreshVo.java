package com.stars.modules.guest.prodata;

/**
 * Created by zhouyaohui on 2017/1/3.
 */
public class GuestRefreshVo {

    private String guestCount;  // 门客数量
    private String guestColor;  // 门客品质数量
    private String refresh;     // 任务品质数量
    private int priority;       // 判断优先级
    private int missionCount;   // 刷新的总数

    public String getGuestCount() {
        return guestCount;
    }

    public void setGuestCount(String guestCount) {
        this.guestCount = guestCount;
    }

    public String getGuestColor() {
        return guestColor;
    }

    public void setGuestColor(String guestColor) {
        this.guestColor = guestColor;
    }

    public String getRefresh() {
        return refresh;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getMissionCount() {
        return missionCount;
    }

    public void setMissionCount(int missionCount) {
        this.missionCount = missionCount;
    }
}
