package com.stars.modules.runeDungeon.userData;

public class FriendHelpChaInfo {
	
	private int dungeonId;//副本id
	
	private int chaStep;//挑战进度
	
	private int angerLevel;//怒气等级
	
	private int killRun;//合作击杀多少轮boss  重置时重置为0

	public int getDungeonId() {
		return dungeonId;
	}

	public void setDungeonId(int dungeonId) {
		this.dungeonId = dungeonId;
	}

	public int getChaStep() {
		return chaStep;
	}

	public void setChaStep(int chaStep) {
		this.chaStep = chaStep;
	}

	public int getAngerLevel() {
		return angerLevel;
	}

	public void setAngerLevel(int angerLevel) {
		this.angerLevel = angerLevel;
	}

	public int getKillRun() {
		return killRun;
	}

	public void setKillRun(int killRun) {
		this.killRun = killRun;
	}

}
