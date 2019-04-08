package com.stars.modules.family.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.family.event.FamilyChangeRedPacketEvent;

/**
 * Created by chenkeyu on 2016/12/1.
 */
public class FamilyChangeRedPacketListener implements EventListener {
    private FamilyModule module;
    public FamilyChangeRedPacketListener(FamilyModule module){
        this.module=module;
    }
    @Override
    public void onEvent(Event event) {
        FamilyChangeRedPacketEvent redPacketEvent = (FamilyChangeRedPacketEvent) event;
        module.setRedPacketCount(redPacketEvent.getRedPacketCount());
    }
}
