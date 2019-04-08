package com.stars.modules.marry.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2017-07-05.
 */
public class SyncSelfDataToTeamEvent extends Event {
    private long roleId;
    private long other;

    public SyncSelfDataToTeamEvent(long roleId, long other) {
        this.roleId = roleId;
        this.other = other;
    }

    public long getRoleId() {
        return roleId;
    }

    public long getOther() {
        return other;
    }
}
