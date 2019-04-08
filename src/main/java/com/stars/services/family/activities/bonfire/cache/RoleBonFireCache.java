package com.stars.services.family.activities.bonfire.cache;

/**
 * Created by wuyuxing on 2017/3/9.
 */
public class RoleBonFireCache {
    private long roleId;
    private int roleLevel;
    private int roleJob;
    private long lastUpdateExpTimes;    //上次计算经验时间
    private boolean canGetWood;
    private long lastUpdateWoodTimes;   //上次刷新干柴时间

    public RoleBonFireCache(long roleId,int roleLevel,int roleJob) {
        this.roleId = roleId;
        this.lastUpdateExpTimes = System.currentTimeMillis();
        this.canGetWood = false;
        this.roleLevel = roleLevel;
        this.roleJob = roleJob;
        this.lastUpdateWoodTimes = System.currentTimeMillis();
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public long getLastUpdateExpTimes() {
        return lastUpdateExpTimes;
    }

    public void setLastUpdateExpTimes(long lastUpdateExpTimes) {
        this.lastUpdateExpTimes = lastUpdateExpTimes;
    }

    public void addLastUpdateExpTimes(long time){
        if(time<=0) return;
        this.lastUpdateExpTimes += time;
    }

    public int getRoleLevel() {
        return roleLevel;
    }

    public int getRoleJob() {
        return roleJob;
    }

    public void setRoleLevel(int roleLevel) {
        this.roleLevel = roleLevel;
    }

    public void setRoleJob(int roleJob) {
        this.roleJob = roleJob;
    }

    public long getLastUpdateWoodTimes() {
        return lastUpdateWoodTimes;
    }

    public void setLastUpdateWoodTimes(long lastUpdateWoodTimes) {
        this.lastUpdateWoodTimes = lastUpdateWoodTimes;
    }

    public boolean isCanGetWood() {
        return canGetWood;
    }

    public void setCanGetWood(boolean canGetWood) {
        this.canGetWood = canGetWood;
    }
}
