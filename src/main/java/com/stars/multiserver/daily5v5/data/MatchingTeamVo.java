package com.stars.multiserver.daily5v5.data;

import java.util.List;

public class MatchingTeamVo {
	
	private int teamId;
	
	private int startTime;
	
	private List<Daily5v5MatchingVo> memberList;
	
	private int integral;//队伍平均修正段位分
	
	private String fightId;

	public int getTeamId() {
		return teamId;
	}

	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}

	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public List<Daily5v5MatchingVo> getMemberList() {
		return memberList;
	}

	public void setMemberList(List<Daily5v5MatchingVo> memberList) {
		this.memberList = memberList;
	}

	public int getIntegral() {
		return integral;
	}

	public void setIntegral(int integral) {
		this.integral = integral;
	}

	public String getFightId() {
		return fightId;
	}

	public void setFightId(String fightId) {
		this.fightId = fightId;
	}

}
