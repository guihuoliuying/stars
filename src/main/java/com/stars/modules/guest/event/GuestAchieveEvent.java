package com.stars.modules.guest.event;

import com.stars.core.event.Event;
import com.stars.modules.guest.userdata.RoleGuest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wuyuxing on 2017/2/9.
 */
public class GuestAchieveEvent extends Event {
    private Map<Integer, RoleGuest> guestMap = new HashMap<>(); // 门客

    public GuestAchieveEvent(Map<Integer, RoleGuest> guestMap) {
        this.guestMap = guestMap;
    }

    public Map<Integer, RoleGuest> getGuestMap() {
        return guestMap;
    }
}
