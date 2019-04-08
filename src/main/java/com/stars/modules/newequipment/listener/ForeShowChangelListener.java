package com.stars.modules.newequipment.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.newequipment.NewEquipmentModule;

/**
 * Created by gaopeidian on 2017/3/3.
 */
public class ForeShowChangelListener implements EventListener {
    private NewEquipmentModule module;
    public ForeShowChangelListener(NewEquipmentModule module){
        this.module = module;
    }
    @Override
    public void onEvent(Event event) {
        module.signCalEquipRedPoint();
        module.flushAllEquipMark();
    }
}
