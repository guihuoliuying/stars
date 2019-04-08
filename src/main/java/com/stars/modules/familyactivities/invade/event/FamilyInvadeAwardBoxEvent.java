package com.stars.modules.familyactivities.invade.event;

import com.stars.core.event.Event;
import com.stars.services.family.activities.invade.cache.AwardBoxCache;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/10/21.
 */
public class FamilyInvadeAwardBoxEvent extends Event {
    private Map<String, AwardBoxCache> boxMap;

    public FamilyInvadeAwardBoxEvent(Map<String, AwardBoxCache> boxMap) {
        this.boxMap = boxMap;
    }

    public Map<String, AwardBoxCache> getBoxMap() {
        return boxMap;
    }
}
