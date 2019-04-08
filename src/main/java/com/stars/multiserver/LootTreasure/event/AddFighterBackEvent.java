package com.stars.multiserver.LootTreasure.event;

import java.util.Set;

public class AddFighterBackEvent {
	
	private int room;
	
	private Set<Long> fighters;
	
	
	public AddFighterBackEvent(int room,Set<Long> fighters){
		this.room = room;
		this.fighters = fighters;
	}


	public int getRoom() {
		return room;
	}


	public void setRoom(int room) {
		this.room = room;
	}


	public Set<Long> getFighters() {
		return fighters;
	}


	public void setFighters(Set<Long> fighters) {
		this.fighters = fighters;
	}
}
