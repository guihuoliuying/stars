package com.stars.modules.skyrank.event;

import com.stars.core.event.Event;

public class SkyRankLogEvent extends Event {
	
	private String info;

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

}
