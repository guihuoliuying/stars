package com.stars.modules.role.event;

import com.stars.core.event.Event;

/**
 * 成就达成检测专用
 * Created by zhanghaizhen on 2017/8/11.
 */
public class RoleLevelAchieveEvent extends Event {

    private int preLevel;
    private int newLevel;

    public RoleLevelAchieveEvent(int newLevel, int preLevel) {
        this.newLevel = newLevel;
        this.preLevel = preLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public void setNewLevel(int newLevel) {
        this.newLevel = newLevel;
    }

    public int getPreLevel() {
        return preLevel;
    }

    public void setPreLevel(int preLevel) {
        this.preLevel = preLevel;
    }
}
