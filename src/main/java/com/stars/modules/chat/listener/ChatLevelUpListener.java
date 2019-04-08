package com.stars.modules.chat.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.role.event.RoleLevelUpEvent;

public class ChatLevelUpListener extends AbstractEventListener<Module> {
	
	public ChatLevelUpListener(Module m){
		super(m);
	}
	@Override
    public void onEvent(Event event) {
		RoleLevelUpEvent le = (RoleLevelUpEvent)event;
		if (le.getNewLevel() >= 5) {
//          ServiceHelper.chatService().putWorldChannelChater(module().id());
		}
    }
}
