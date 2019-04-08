package com.stars.modules.family.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2016/11/30.
 */
public class FamilyRemoveApplyEvent extends Event {
    private long applyId;
    public FamilyRemoveApplyEvent(long applyId){
        this.applyId=applyId;
    }

    public long getApplyId() {
        return applyId;
    }
}
