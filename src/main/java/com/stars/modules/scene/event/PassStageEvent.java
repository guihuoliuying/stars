package com.stars.modules.scene.event;

import com.stars.core.event.Event;

public class PassStageEvent extends Event {
	private int stageId;
	private int star;
	private boolean isFirstPass;
	
	public PassStageEvent(int stageId, int star, boolean isFirstPass){
		this.stageId = stageId;
		this.star = star;
		this.isFirstPass = isFirstPass;
	}

	public int getStageId() {
		return stageId;
	}

	public void setStageId(int stageId) {
		this.stageId = stageId;
	}

	public int getStar() {
		return star;
	}
	
	public boolean getIsFirstPass(){
		return isFirstPass;
	}
}
