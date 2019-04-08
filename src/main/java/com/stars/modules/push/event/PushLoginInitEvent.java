package com.stars.modules.push.event;

import com.stars.core.event.Event;

/**
 * Created by zhaowenshuo on 2017/7/19.
 */
public class PushLoginInitEvent extends Event {
    private boolean isReset;

    public PushLoginInitEvent(boolean isReset) {
        this.isReset = isReset;
    }

    public boolean isReset() {
        return isReset;
    }
}
