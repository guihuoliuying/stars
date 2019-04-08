package com.stars.modules.email.event;

import com.stars.core.event.Event;

/**
 * Created by zhaowenshuo on 2017/4/15.
 */
public class EmailRedPointEvent extends Event {

    private int untreatedCount = 0;

    public EmailRedPointEvent(int untreatedCount) {
        this.untreatedCount = untreatedCount;
    }

    public int getUntreatedCount() {
        return untreatedCount;
    }
}
