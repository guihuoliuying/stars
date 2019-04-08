package com.stars.modules.familyactivities.treasure.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.familyactivities.treasure.FamilyTreasureModule;
import com.stars.modules.familyactivities.treasure.event.FamilyTreasureStageEvent;

/**
 * Created by chenkeyu on 2017/2/13 11:48
 */
public class FamilyTreasureListener implements EventListener {
    private FamilyTreasureModule FTModule;

    public FamilyTreasureListener(FamilyTreasureModule FTModule) {
        this.FTModule = FTModule;
    }

    @Override
    public void onEvent(Event event) {
        FamilyTreasureStageEvent stageEvent = (FamilyTreasureStageEvent) event;
        FTModule.doEvent(stageEvent.getLevel(), stageEvent.getStep(), stageEvent.getDamage(),
                stageEvent.getTotalDamage(), stageEvent.getRank(), stageEvent.getStartType(), stageEvent.isFlushToClient());
        if (stageEvent.isResetDamage()){
            FTModule.doResetEvent();
        }
    }
}
