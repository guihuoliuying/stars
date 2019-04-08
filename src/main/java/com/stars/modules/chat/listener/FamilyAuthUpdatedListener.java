package com.stars.modules.chat.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.chat.ChatModule;
import com.stars.modules.family.event.FamilyAuthUpdatedEvent;

/**
 * Created by chenkeyu on 2017/1/11 11:33
 */
public class FamilyAuthUpdatedListener implements EventListener {
    private ChatModule module;
    public FamilyAuthUpdatedListener(ChatModule module){
        this.module = module;
    }
    @Override
    public void onEvent(Event event) {
        FamilyAuthUpdatedEvent updatedEvent = (FamilyAuthUpdatedEvent) event;
        if (updatedEvent.getFamilyId() != 0) {
            module.addFamilyMemberId(updatedEvent.getFamilyId());
        }
    }
}
