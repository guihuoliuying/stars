package com.stars.modules.email.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2016/11/29.
 */
public class RemoveEmailEvent extends Event {
    private int emailId;

    public RemoveEmailEvent(int emailId) {
        this.emailId = emailId;
    }

    public RemoveEmailEvent() {
    }

    public int getEmailId() {
        return emailId;
    }
}
