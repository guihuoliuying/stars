package com.stars.modules.role.event;

import com.stars.core.event.Event;

/**
 * 成就达成检测用
 * Created by zhanghaizhen on 2017/8/11.
 */
public class FightScoreAchieveEvent extends Event {
    private long roleId;
    private int preFightScore;
    private int newFightScore;
    private int roleLevel;

    public FightScoreAchieveEvent(long roleId, int preFightScore, int newFightScore, int roleLevel) {
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
