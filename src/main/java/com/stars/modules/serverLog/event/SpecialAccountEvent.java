package com.stars.modules.serverLog.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2017-03-27 10:41
 */
public class SpecialAccountEvent extends Event {
    private long roleId;
    private String content;
    private boolean self;

    public SpecialAccountEvent(long roleId, String content, boolean self) {
        this.roleId = roleId;
        this.content = content;
        this.self = self;
    }

    public long getRoleId() {
        return roleId;
    }

    public String getContent() {
        return content;
    }

    public boolean isSelf() {
        return self;
    }
}
