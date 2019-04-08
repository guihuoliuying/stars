package com.stars.modules.daily.prodata;

/**
 * Created by zhanghaizhen on 2017/7/12.
 */
public class DailyFightScoreModuleVo {

    private String sysName; //模块系统名
    private int openDays; //开服天数
    private int recommFightScore; //推荐战力

    public String getSysName() {
        return sysName;
    }

    public void setSysName(String sysName) {
        this.sysName = sysName;
    }

    public int getOpenDays() {
        return openDays;
    }

    public void setOpenDays(int openDays) {
        this.openDays = openDays;
    }

    public int getRecommFightScore() {
        return recommFightScore;
    }

    public void setRecommFightScore(int recommFightScore) {
        this.recommFightScore = recommFightScore;
    }
}
