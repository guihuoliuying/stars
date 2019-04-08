package com.stars.modules.hotUpdate.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.hotUpdate.HotUpdateModule;
import com.stars.modules.hotUpdate.event.*;

/**
 * Created by wuyuxing on 2017/1/4.
 */
public class HotUpdateEventListener extends AbstractEventListener<Module> {

    private HotUpdateModule hotUpdateModule;

    public HotUpdateEventListener(Module module) {
        super(module);
        this.hotUpdateModule = (HotUpdateModule) module;
    }

    @Override
    public void onEvent(Event event) {
        if(event instanceof HotUpdateAddItemEvent){
            HotUpdateAddItemEvent addItemEvent = (HotUpdateAddItemEvent) event;
            hotUpdateModule.handleAddItemEvent(addItemEvent);
        }else if(event instanceof HotUpdateDeleteItemEvent){
            HotUpdateDeleteItemEvent deleteEvent = (HotUpdateDeleteItemEvent) event;
            hotUpdateModule.handleDeleteItemEvent(deleteEvent);
        }else if(event instanceof HotUpdateBaseEvent){
            HotUpdateBaseEvent baseEvent = (HotUpdateBaseEvent) event;
            hotUpdateModule.handleBaseEvent(baseEvent.getData());
        }else if(event instanceof HotUpdateCommEvent){
            HotUpdateCommEvent commEvent = (HotUpdateCommEvent) event;
            hotUpdateModule.handleCommEvent(commEvent.getData());
        }else if(event instanceof HotUpdateStandyEvent){
            HotUpdateStandyEvent standyEvent = (HotUpdateStandyEvent) event;
            hotUpdateModule.handleStandyEvent(standyEvent.getData());
        }
    }
}
