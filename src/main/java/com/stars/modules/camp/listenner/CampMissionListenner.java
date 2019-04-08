package com.stars.modules.camp.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.camp.CampModule;
import com.stars.modules.scene.event.PassStageEvent;

/**
 * Created by huwenjun on 2017/7/5.
 */
public class CampMissionListenner extends AbstractEventListener<CampModule> {
    public CampMissionListenner(CampModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof PassStageEvent) {//
            PassStageEvent passStageEvent = (PassStageEvent) event;
            module().checkMissionState(2, passStageEvent.getStageId());
        }
    }
}
