package com.stars.services.friend.memdata;

/**
 * Created by zhaowenshuo on 2016/8/12.
 */
public class RecommendationFriend {

    private long roleId;
    private String name;
    private int jobId;
    private int level;
    private int fightScore;
    private int offlineTimestamp;

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getFightScore() {
        return fightScore;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public int getOfflineTimestamp() {
        return offlineTimestamp;
    }

    public void setOfflineTimestamp(int offlineTimestamp) {
        this.offlineTimestamp = offlineTimestamp;
    }

    @Override
    public String toString() {
        return "recom(" +
                "roleId=" + roleId +
                ", name='" + name + '\'' +
                ", jobId=" + jobId +
                ", level=" + level +
                ", fightScore=" + fightScore +
                ", offlineTimestamp=" + offlineTimestamp +
                ')';
    }
}
