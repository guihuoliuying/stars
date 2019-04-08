package com.stars.modules.elitedungeon.event;

import com.stars.core.event.Event;

public class EliteDungeonAddImageDataEvent extends Event {
	
	private int stageId;

	public int getStageId() {
		return stageId;
	}

	public void setStageId(int stageId) {
		this.stageId = stageId;
	}

}
