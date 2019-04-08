package com.stars.modules.tool.event;

import com.stars.core.event.Event;

public class UseUnlockEquipToolEvent extends Event {
	private int itemId;

	public UseUnlockEquipToolEvent(int itemId){
		this.itemId = itemId;
	}
	
	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
}
