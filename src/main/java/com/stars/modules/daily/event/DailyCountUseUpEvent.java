package com.stars.modules.daily.event;

import com.stars.core.event.Event;

public class DailyCountUseUpEvent extends Event {
	private short dailyId;

	public DailyCountUseUpEvent(short dailyId){
		this.dailyId = dailyId;
	}
	public short getDailyId() {
		return dailyId;
	}
	public void setDailyId(short dailyId) {
		this.dailyId = dailyId;
	}
}
