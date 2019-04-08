package com.stars.multiserver.daily5v5.data;

public class Daily5v5FightData {
	
	private String fightId;
	
	private MatchingTeamVo team1;
	
	private MatchingTeamVo team2;
	
	private int fightServerId;
	
	private long creatTimestamp;

	public String getFightId() {
		return fightId;
	}

	public void setFightId(String fightId) {
		this.fightId = fightId;
	}

	public MatchingTeamVo getTeam1() {
		return team1;
	}

	public void setTeam1(MatchingTeamVo team1) {
		this.team1 = team1;
	}

	public MatchingTeamVo getTeam2() {
		return team2;
	}

	public void setTeam2(MatchingTeamVo team2) {
		this.team2 = team2;
	}

	public int getFightServerId() {
		return fightServerId;
	}

	public void setFightServerId(int fightServerId) {
		this.fightServerId = fightServerId;
	}

	public long getCreatTimestamp() {
		return creatTimestamp;
	}

	public void setCreatTimestamp(long creatTimestamp) {
		this.creatTimestamp = creatTimestamp;
	}

}
