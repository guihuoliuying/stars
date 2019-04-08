package com.stars.modules.pk.event;

import com.stars.core.event.Event;

/**
 * Created by daiyaorong on 2016/9/2.
 */
public class EnterPkEvent extends Event {

    private long invitor;
    private long invitee;
    private byte[] data;

    public EnterPkEvent() {
    }

    public long getInvitor() {
        return invitor;
    }

    public void setInvitor(long invitor) {
        this.invitor = invitor;
    }

    public long getInvitee() {
        return invitee;
    }

    public void setInvitee(long invitee) {
        this.invitee = invitee;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
