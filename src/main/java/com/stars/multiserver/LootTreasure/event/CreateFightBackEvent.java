package com.stars.multiserver.LootTreasure.event;

public class CreateFightBackEvent {
	private int room;
	private String fightActor;
	public CreateFightBackEvent(int room,String fightActor){
		this.room = room;
		this.fightActor = fightActor;
	}
	public int getRoom() {
		return room;
	}
	public void setRoom(int room) {
		this.room = room;
	}
	public String getFightActor() {
		return fightActor;
	}
	public void setFightActor(String fightActor) {
		this.fightActor = fightActor;
	}
}
