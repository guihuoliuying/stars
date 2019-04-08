package com.stars.modules.daily5v5.event;

import com.stars.core.event.Event;

public class Daily5v5MatchingSuccessEvent extends Event {
	
	public Daily5v5MatchingSuccessEvent(int continueFihgtServerId) {
		this.continueFihgtServerId = continueFihgtServerId;
	}
	
	private int continueFihgtServerId;

	public int getContinueFihgtServerId() {
		return continueFihgtServerId;
	}

	public void setContinueFihgtServerId(int continueFihgtServerId) {
		this.continueFihgtServerId = continueFihgtServerId;
	}

}
