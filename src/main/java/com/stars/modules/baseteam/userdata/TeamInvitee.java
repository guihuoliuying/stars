package com.stars.modules.baseteam.userdata;

public class TeamInvitee {
	
	private long id;
	private short level;
	private String name;
	private byte job;
	private int fightScore;
	
	public TeamInvitee(){
		
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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
	 
}
