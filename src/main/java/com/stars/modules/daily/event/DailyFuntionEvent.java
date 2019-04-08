package com.stars.modules.daily.event;

import com.stars.core.event.Event;

public class DailyFuntionEvent extends Event {
	private short dailyId;
	private int count;
	public DailyFuntionEvent(short dailyId,int count){
		this.dailyId = dailyId;
		this.count = count;
	}
	public short getDailyId() {
		return dailyId;
	}
	public void setDailyId(short dailyId) {
		this.dailyId = dailyId;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
}
