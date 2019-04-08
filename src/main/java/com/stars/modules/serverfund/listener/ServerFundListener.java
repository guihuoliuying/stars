package com.stars.modules.serverfund.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.serverfund.ServerFundModule;

public class ServerFundListener extends AbstractEventListener<ServerFundModule> {

	public ServerFundListener(ServerFundModule module) {
		super(module);
	}
	
	@Override
	public void onEvent(Event event) {
		module().handleEvent(event);
	}

}
