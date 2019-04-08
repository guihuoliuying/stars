package com.stars.modules.newequipment.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.changejob.event.ChangeJobEvent;
import com.stars.modules.newequipment.NewEquipmentModule;

/**
 * Created by huwenjun on 2017/6/8.
 */
public class NewEquipChangeJobListenner extends AbstractEventListener<NewEquipmentModule> {
    public NewEquipChangeJobListenner(NewEquipmentModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof ChangeJobEvent) {
            ChangeJobEvent changeJobEvent = (ChangeJobEvent) event;
            module().onChangeJob(changeJobEvent.getNewJobId());
        }
    }
}
