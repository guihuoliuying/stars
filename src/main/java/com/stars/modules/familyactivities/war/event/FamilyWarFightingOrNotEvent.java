package com.stars.modules.familyactivities.war.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2017-06-10.
 */
public class FamilyWarFightingOrNotEvent extends Event {
    private boolean fightOrNot;

    public FamilyWarFightingOrNotEvent(boolean fightOrNot) {
        this.fightOrNot = fightOrNot;
    }

    public boolean isFightOrNot() {
        return fightOrNot;
    }
}
