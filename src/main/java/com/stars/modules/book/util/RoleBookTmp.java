package com.stars.modules.book.util;

/**
 * Created by zhoujin on 2017/5/10.
 */
public class RoleBookTmp {
    private long roleId;
    private String name;
    private int level;
    private int jobId;
    private int beLastKickTime;
    //private short kickTimes;
    private short beKickTimes;
    private byte icon;

    public long getRoleId() {return this.roleId;}

    public int getJobId() {
        return jobId;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getBeLastKickTime() {
        return beLastKickTime;
    }

    public void setBeLastKickTime(int beLastKickTime) {
        this.beLastKickTime = beLastKickTime;
    }

    public short getBeKickTimes() {
        return beKickTimes;
    }

    public void setBeKickTimes(short beKickTimes) {
        this.beKickTimes = beKickTimes;
    }

    public byte getIcon() {
        return icon;
    }

    public void setIcon(byte icon) {
        this.icon = icon;
    }
}
