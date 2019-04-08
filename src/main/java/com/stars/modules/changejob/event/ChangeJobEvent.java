package com.stars.modules.changejob.event;

import com.stars.core.event.Event;

/**
 * Created by huwenjun on 2017/6/2.
 */
public class ChangeJobEvent extends Event {
    private Integer newJobId;

    public ChangeJobEvent() {
    }

    public ChangeJobEvent(Integer newJobId) {
        this.newJobId = newJobId;
    }

    public Integer getNewJobId() {
        return newJobId;
    }

    public void setNewJobId(Integer newJobId) {
        this.newJobId = newJobId;
    }
}
