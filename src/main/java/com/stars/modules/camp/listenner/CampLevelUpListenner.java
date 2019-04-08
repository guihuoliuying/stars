package com.stars.modules.camp.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.camp.CampModule;
import com.stars.modules.camp.event.CampLevelUpEvent;

/**
 * Created by huwenjun on 2017/6/27.
 */
public class CampLevelUpListenner extends AbstractEventListener<CampModule> {
    public CampLevelUpListenner(CampModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof CampLevelUpEvent) {
            CampLevelUpEvent campLevelUpEvent = (CampLevelUpEvent) event;
            module().handleCampLevelUp(campLevelUpEvent);
            module().refreshMissionList();
        }
    }
}
