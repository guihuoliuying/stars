package com.stars.modules.push.event;

import com.stars.core.event.Event;

/**
 * fixme: 应该批量抛出激活的pushId
 * Created by zhaowenshuo on 2017/3/28.
 */
public class PushInactivedEvent extends Event {

    private int pushId;

    public PushInactivedEvent(int pushId) {
        this.pushId = pushId;
    }

    public int getPushId() {
        return pushId;
    }
}
