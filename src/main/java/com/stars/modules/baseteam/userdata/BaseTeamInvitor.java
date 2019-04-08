package com.stars.modules.baseteam.userdata;

/**
 * Created by liuyuheng on 2016/11/10.
 */
public class BaseTeamInvitor {
    private long invitorId;
    private short level;
    private String name;
    private byte job;
    private int fightScore;
    private int teamId;
    private byte memberCount;// 队伍当前人数
    private byte maxMemberCount;// 队伍最大人数
    private byte teamType;// 队伍类型
    private int target;// 队伍目标

    public BaseTeamInvitor() {
    }

    public long getInvitorId() {
        return invitorId;
    }

    public void setInvitorId(long invitorId) {
        this.invitorId = invitorId;
    }

    public short getLevel() {
        return level;
    }

    public void setLevel(short level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getJob() {
        return job;
    }

    public void setJob(byte job) {
        this.job = job;
    }

    public int getFightScore() {
        return fightScore;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public byte getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(byte memberCount) {
        this.memberCount = memberCount;
    }

    public byte getTeamType() {
        return teamType;
    }

    public void setTeamType(byte teamType) {
        this.teamType = teamType;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public byte getMaxMemberCount() {
        return maxMemberCount;
    }

    public void setMaxMemberCount(byte maxMemberCount) {
        this.maxMemberCount = maxMemberCount;
    }

    @Override
    public String toString() {
        return "BaseTeamInvitor{" +
                "invitorId=" + invitorId +
                ", level=" + level +
                ", name='" + name + '\'' +
                ", job=" + job +
                ", fightScore=" + fightScore +
                ", teamId=" + teamId +
                ", memberCount=" + memberCount +
                ", maxMemberCount=" + maxMemberCount +
                ", teamType=" + teamType +
                ", target=" + target +
                '}';
    }
}
