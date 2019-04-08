package com.stars.modules.email.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.email.EmailModule;
import com.stars.modules.email.event.SpecialEmailEvent;

public class SpecialEmailListener extends AbstractEventListener<EmailModule> {

	public SpecialEmailListener(EmailModule module) {
		super(module);
	}
	
	@Override
	public void onEvent(Event event) {
		if(event instanceof SpecialEmailEvent){
			SpecialEmailEvent specialEmailEvent = (SpecialEmailEvent)event;
			module().handleSpecialEmail(specialEmailEvent.getEmailType(), specialEmailEvent.getEmailId());
		}
	}

}
