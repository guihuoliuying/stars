package com.stars.multiserver.daily5v5.data;

public class Daily5v5FightingInfo {
	
	private String fightId;
	
	private int teamId;
	
	public Daily5v5FightingInfo() {
		// TODO Auto-generated constructor stub
	}

	public Daily5v5FightingInfo(String fightId, int teamId) {
		super();
		this.fightId = fightId;
		this.teamId = teamId;
	}

	public String getFightId() {
		return fightId;
	}

	public void setFightId(String fightId) {
		this.fightId = fightId;
	}

	public int getTeamId() {
		return teamId;
	}

	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}

}
