package com.stars.modules.friend.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.friend.event.FriendLogEvent;

public class FriendLogListener implements EventListener {
	
	private FriendModule module;

    public FriendLogListener(FriendModule module) {
        this.module = module;
    }

	@Override
	public void onEvent(Event event) {
		FriendLogEvent friendLogEvent = (FriendLogEvent)event;
		module.friendLog(friendLogEvent);
	}

}
