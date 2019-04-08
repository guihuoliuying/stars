package com.stars.modules.daily5v5.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.daily5v5.Daily5v5Module;
import com.stars.modules.daily5v5.event.Daily5v5CancelMatchingEvent;
import com.stars.modules.daily5v5.event.Daily5v5FightEndEvent;
import com.stars.modules.daily5v5.event.Daily5v5MatchingSuccessEvent;
import com.stars.modules.daily5v5.event.Daily5v5MessageEvent;

public class Daily5v5Lisener extends AbstractEventListener<Module> {
	
	private Daily5v5Module daily5v5Module;

	public Daily5v5Lisener(Module module) {
		super(module);
		daily5v5Module = (Daily5v5Module)module;
	}
	
	@Override
	public void onEvent(Event event) {
		if(event instanceof Daily5v5MatchingSuccessEvent){
			Daily5v5MatchingSuccessEvent dEvent = (Daily5v5MatchingSuccessEvent)event;
			daily5v5Module.finishMatching(dEvent.getContinueFihgtServerId());
		}else if(event instanceof Daily5v5FightEndEvent){
			Daily5v5FightEndEvent daily5v5FightEndEvent = (Daily5v5FightEndEvent)event;
			daily5v5Module.finishFight(daily5v5FightEndEvent.getPacket(), daily5v5FightEndEvent.getResult(), daily5v5FightEndEvent.getTime());
		}else if(event instanceof Daily5v5MessageEvent){
			daily5v5Module.continueButFightEnd();
		}else if(event instanceof Daily5v5CancelMatchingEvent){
			daily5v5Module.cancelSuccess();
		}
	}

}
