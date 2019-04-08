package com.stars.modules.ride.event;

import com.stars.core.event.Event;

/**
 * Created by zhaowenshuo on 2016/10/19.
 */
public class RideLevelUpEvent extends Event {

	private int prevLevelId;
	private int currLevelId;

	public RideLevelUpEvent(int prevLevelId, int currLevelId) {
		this.prevLevelId = prevLevelId;
		this.currLevelId = currLevelId;
	}

	public int getPrevLevelId() {
		return prevLevelId;
	}

	public void setPrevLevelId(int prevLevelId) {
		this.prevLevelId = prevLevelId;
	}

	public int getCurrLevelId() {
		return currLevelId;
	}

	public void setCurrLevelId(int currLevelId) {
		this.currLevelId = currLevelId;
	}
}
