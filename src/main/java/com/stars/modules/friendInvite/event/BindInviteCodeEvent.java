package com.stars.modules.friendInvite.event;

import com.stars.core.event.Event;

/**
 * Created by chenxie on 2017/6/12.
 */
public class BindInviteCodeEvent extends Event {

    /**
     * 邀请方角色ID
     */
    private long inviterId;
    private long inviteeId;

    public BindInviteCodeEvent(long inviterId, long inviteeId) {
        this.inviterId = inviterId;
        this.inviteeId = inviteeId;
    }

    public long getInviterId() {
        return inviterId;
    }

    public void setInviterId(long inviterId) {
        this.inviterId = inviterId;
    }

    public long getInviteeId() {
        return inviteeId;
    }

    public void setInviteeId(long inviteeId) {
        this.inviteeId = inviteeId;
    }
}
