package com.stars.modules.newredbag.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.family.event.FamilyAuthUpdatedEvent;
import com.stars.modules.newredbag.NewRedbagModule;

/**
 * Created by zhouyaohui on 2017/2/21.
 */
public class FamilyAuthUpdateListener extends AbstractEventListener {
    public FamilyAuthUpdateListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        NewRedbagModule module = (NewRedbagModule) module();
        FamilyAuthUpdatedEvent e = (FamilyAuthUpdatedEvent) event;
        module.updateFamilyAuth(e.getFamilyId(), e.getPrevFamilyId());
    }
}
