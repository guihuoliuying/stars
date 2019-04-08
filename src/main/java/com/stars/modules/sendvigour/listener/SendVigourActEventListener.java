package com.stars.modules.sendvigour.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.sendvigour.SendVigourModule;
import com.stars.modules.sendvigour.event.SendVigourActEvent;

/**
 * Created by gaopeidian on 2017/3/30.
 */
public class SendVigourActEventListener extends AbstractEventListener<Module> {

    public SendVigourActEventListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
    	SendVigourModule sendVigourModule = (SendVigourModule)this.module();
    	if (event instanceof SendVigourActEvent) {
			SendVigourActEvent sendVigourActEvent = (SendVigourActEvent)event;
			sendVigourModule.handleSendVigourActEvent(sendVigourActEvent);
		}
    }

}
