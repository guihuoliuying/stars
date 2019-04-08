package com.stars.multiserver.teamPVPGame.helper;

public abstract class TPGTask{
	long time;	
	public TPGTask(long time){
		this.time = time;
	}
	abstract public void doTask();
	
	public long getTime() {
		return time;
	}
}
