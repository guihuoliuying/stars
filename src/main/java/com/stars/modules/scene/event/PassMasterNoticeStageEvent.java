package com.stars.modules.scene.event;

import com.stars.core.event.Event;

public class PassMasterNoticeStageEvent extends Event {
	private int stageId;
	
	public PassMasterNoticeStageEvent(int stageId){
		this.stageId = stageId;
	}

	public int getStageId() {
		return stageId;
	}

	public void setStageId(int stageId) {
		this.stageId = stageId;
	}
}
