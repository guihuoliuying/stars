package com.stars.modules.camp.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.camp.CampModule;
import com.stars.modules.camp.event.RareOfficerResetEvent;

/**
 * Created by huwenjun on 2017/6/30.
 */
public class RoleOfficerListenner extends AbstractEventListener<CampModule> {
    public RoleOfficerListenner(CampModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof RareOfficerResetEvent) {
            module().checkAndResetReputation();
            module().queryMyRareOfficer();
        }
    }
}
