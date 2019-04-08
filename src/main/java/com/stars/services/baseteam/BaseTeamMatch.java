package com.stars.services.baseteam;

/**
 * Created by zhouyaohui on 2017/2/24.
 */
public class BaseTeamMatch {
    public final static byte MATCH_TEAM = 1;
    public final static byte MATCH_MEMBER = 2;

    private int target;
    private byte teamType;
    private int stamp;
    private long roleId;
    private byte matchType;
    private int eliteMatchTimes;//精英副本匹配时间

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public byte getTeamType() {
        return teamType;
    }

    public void setTeamType(byte teamType) {
        this.teamType = teamType;
    }

    public int getStamp() {
        return stamp;
    }

    public void setStamp(int stamp) {
        this.stamp = stamp;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public byte getMatchType() {
        return matchType;
    }

    public void setMatchType(byte matchType) {
        this.matchType = matchType;
    }

	public int getEliteMatchTimes() {
		return eliteMatchTimes;
	}

	public void setEliteMatchTimes(int eliteMatchTimes) {
		this.eliteMatchTimes = eliteMatchTimes;
	}
}
