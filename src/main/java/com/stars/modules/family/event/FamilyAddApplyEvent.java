package com.stars.modules.family.event;

import com.stars.core.event.Event;

import java.util.Set;

/**
 * Created by chenkeyu on 2016/11/30.
 */
public class FamilyAddApplyEvent extends Event {
    private Set<Long> applyIds;
    private long applyId;
    public FamilyAddApplyEvent(long applyId){this.applyId=applyId;}

    public FamilyAddApplyEvent(Set<Long> applyIds) {
        this.applyIds = applyIds;
    }

    public long getApplyId() {
        return applyId;
    }

    public Set<Long> getApplyIds() {
        return applyIds;
    }
}
