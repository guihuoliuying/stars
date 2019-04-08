package com.stars.modules.familyactivities.treasure.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2017-03-10 11:19
 */
public class LeaveOrKickOutFamilyEvent extends Event {
    private boolean canVerify;

    public LeaveOrKickOutFamilyEvent(boolean canVerify) {
        this.canVerify = canVerify;
    }

    public boolean isCanVerify() {
        return canVerify;
    }
}
