package com.stars.modules.role.event;

import com.stars.core.event.Event;

/**
 * Created by liuyuheng on 2016/8/25.
 */
public class FightScoreChangeEvent extends Event {
    private long roleId;
    private int preFightScore;
    private int newFightScore;
    private int roleLevel;

    public FightScoreChangeEvent(long roleId, int preFightScore, int newFightScore, int roleLevel) {
        this.roleId = roleId;
        this.preFightScore = preFightScore;
        this.newFightScore = newFightScore;
        this.roleLevel = roleLevel;
    }

    public long getRoleId() {
        return roleId;
    }

    public int getPreFightScore() {
        return preFightScore;
    }

    public int getNewFightScore() {
        return newFightScore;
    }

    public int getRoleLevel() {
        return roleLevel;
    }
}
