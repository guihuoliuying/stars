package com.stars.modules.familyactivities.treasure.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.familyactivities.treasure.FamilyTreasureModule;

/**
 * Created by chenkeyu on 2017-03-10 11:25
 */
public class LeaveOrKickOutFamilyListener implements EventListener {
    private FamilyTreasureModule module;

    public LeaveOrKickOutFamilyListener(FamilyTreasureModule module) {
        this.module = module;
    }

    @Override
    public void onEvent(Event event) {
        module.doChangeFamily();
    }
}
