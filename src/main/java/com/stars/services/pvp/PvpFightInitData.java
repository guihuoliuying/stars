package com.stars.services.pvp;

/**
 * Created by zhaowenshuo on 2016/11/21.
 */
public class PvpFightInitData {

    private long inviterId;
    private long inviteeId;

    public PvpFightInitData(long inviterId, long inviteeId) {
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
