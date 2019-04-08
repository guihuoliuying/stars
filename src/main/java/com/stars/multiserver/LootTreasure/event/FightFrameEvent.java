package com.stars.multiserver.LootTreasure.event;

import com.stars.multiserver.fight.data.LuaFrameData;

public class FightFrameEvent {
	private int room;
	private LuaFrameData lFrameData;
	
	public FightFrameEvent(int room,LuaFrameData luaFrameData){
		this.room = room;
		this.lFrameData = luaFrameData;
	}
	
	public int getRoom() {
		return room;
	}
	public void setRoom(int room) {
		this.room = room;
	}
	public LuaFrameData getlFrameData() {
		return lFrameData;
	}
	public void setlFrameData(LuaFrameData lFrameData) {
		this.lFrameData = lFrameData;
	}
	
}
