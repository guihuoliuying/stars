package com.stars.modules.induct.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.induct.InductModule;
import com.stars.modules.induct.event.InductEvent;

/**
 * Created gaopeidian on 2017/3/28.
 */
public class InductListener extends AbstractEventListener<Module> {

    public InductListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
    	InductEvent inductEvent = (InductEvent)event;
        InductModule module = (InductModule)this.module();        
        module.handleInductEvent(inductEvent);
    }
}
