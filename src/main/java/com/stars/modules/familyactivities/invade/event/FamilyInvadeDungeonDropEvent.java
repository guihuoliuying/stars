package com.stars.modules.familyactivities.invade.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/10/21.
 */
public class FamilyInvadeDungeonDropEvent extends Event {
    private Map<String, Integer> dropIds;

    public FamilyInvadeDungeonDropEvent(Map<String, Integer> dropIds) {
        this.dropIds = dropIds;
    }

    public Map<String, Integer> getDropIds() {
        return dropIds;
    }
}
