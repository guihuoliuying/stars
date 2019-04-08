package com.stars.modules.camp.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.camp.CampModule;
import com.stars.modules.camp.event.ActivityFinishEvent;
import com.stars.modules.camp.event.MissionFinishEvent;

/**
 * Created by huwenjun on 2017/7/7.
 */
public class ActivityOrMissionListenner extends AbstractEventListener<CampModule> {
    public ActivityOrMissionListenner(CampModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof ActivityFinishEvent) {
            module().onActivityFinish((ActivityFinishEvent) event);
        }
        if (event instanceof MissionFinishEvent) {
            module().onMissionFinish((MissionFinishEvent) event);
        }
    }
}
