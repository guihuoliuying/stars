package com.stars.modules.newequipment.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.newequipment.NewEquipmentModule;

/**
 * Created by chenkeyu on 2016/12/20.
 */
public class UseToolListener implements EventListener {
    private NewEquipmentModule module;
    public UseToolListener(NewEquipmentModule module){
        this.module = module;
    }
    @Override
    public void onEvent(Event event) {
        module.signCalEquipRedPoint();
    }
}
