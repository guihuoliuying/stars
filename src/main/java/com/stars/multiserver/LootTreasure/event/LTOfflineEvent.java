package com.stars.multiserver.LootTreasure.event;

public class LTOfflineEvent {
	private int room;
	private long roleId;
	public LTOfflineEvent(int room,long roleId){
		this.room = room;
		this.roleId = roleId;
	}
	public int getRoom() {
		return room;
	}
	public void setRoom(int room) {
		this.room = room;
	}
	public long getRoleId() {
		return roleId;
	}
	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}
}
