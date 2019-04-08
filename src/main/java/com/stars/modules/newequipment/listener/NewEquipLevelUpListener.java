package com.stars.modules.newequipment.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.newequipment.NewEquipmentModule;
import com.stars.modules.role.event.RoleLevelUpEvent;

/**
 * Created by wuyuxing on 2016/12/12.
 */
public class NewEquipLevelUpListener extends AbstractEventListener<Module> {

    public NewEquipLevelUpListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        RoleLevelUpEvent levelUpEvent = (RoleLevelUpEvent) event;
        NewEquipmentModule module = (NewEquipmentModule) this.module();
        module.signCalEquipRedPoint();
        module.flushAllEquipMark();
    }
}
