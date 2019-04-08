package com.stars.modules.pk.event;

import com.stars.core.event.Event;
import com.stars.modules.pk.userdata.InvitorCache;

/**
 * Created by daiyaorong on 2016/9/2.
 */
public class InvitePkEvent extends Event {

    private Long invitorId;
    private Long invitedId;
    private InvitorCache invitorCache;// 邀请者信息

    public InvitePkEvent() {
    }

    public InvitorCache getInvitorCache() {
        return invitorCache;
    }

    public void setInvitorCache(InvitorCache invitorCache) {
        this.invitorCache = invitorCache;
    }

    public Long getInvitorId() {
        return invitorId;
    }

    public void setInvitorId(Long invitorId) {
        this.invitorId = invitorId;
    }

    public Long getInvitedId() {
        return invitedId;
    }

    public void setInvitedId(Long invitedId) {
        this.invitedId = invitedId;
    }
}
