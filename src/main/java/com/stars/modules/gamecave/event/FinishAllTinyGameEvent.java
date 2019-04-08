package com.stars.modules.gamecave.event;

import com.stars.core.event.Event;

/**
 * Created by gaopeidian on 2016/9/19.
 */
public class FinishAllTinyGameEvent extends Event {
    private long roleId;
    private int tinyGameScore;
 
    public FinishAllTinyGameEvent(long roleId, int tinyGameScore) {
        this.roleId = roleId;
        this.tinyGameScore = tinyGameScore;
    }

    public long getRoleId() {
        return roleId;
    }

    public int getTinyGameScore() {
        return tinyGameScore;
    }
}
