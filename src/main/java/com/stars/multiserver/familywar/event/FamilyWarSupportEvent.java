package com.stars.multiserver.familywar.event;

import com.stars.core.event.Event;

public class FamilyWarSupportEvent extends Event {
    private int warType;

    public FamilyWarSupportEvent(int warType) {
        this.warType = warType;
    }

    public int getWarType() {
        return warType;
    }
}
