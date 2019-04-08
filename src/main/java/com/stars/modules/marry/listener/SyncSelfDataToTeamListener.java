package com.stars.modules.marry.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.marry.MarryModule;
import com.stars.modules.marry.event.SyncSelfDataToTeamEvent;

/**
 * Created by chenkeyu on 2017-07-05.
 */
public class SyncSelfDataToTeamListener extends AbstractEventListener {
    public SyncSelfDataToTeamListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        SyncSelfDataToTeamEvent toTeamEvent = (SyncSelfDataToTeamEvent) event;
        MarryModule marry = (MarryModule) module();
        marry.syncSelfData(toTeamEvent.getOther(), toTeamEvent.getRoleId());
    }
}
