package com.stars.modules.newofflinepvp.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.newofflinepvp.NewOfflinePvpModule;
import com.stars.modules.role.event.RoleLevelUpEvent;

/**
 * Created by chenkeyu on 2017-03-15 10:28
 */
public class RoleLevelChangeOfflinePvpListener implements EventListener {
    private NewOfflinePvpModule module;

    public RoleLevelChangeOfflinePvpListener(NewOfflinePvpModule module) {
        this.module = module;
    }

    @Override
    public void onEvent(Event event) {
        RoleLevelUpEvent upEvent = (RoleLevelUpEvent) event;
        module.changeRoleLevel(upEvent.getNewLevel());
    }
}
