package com.stars.modules.role.event;

import com.stars.core.event.Event;

/**
 * 角色升级事件
 * Created by liuyuheng on 2016/6/28.
 */
public class RoleLevelUpEvent extends Event {

    private int preLevel;
    private int newLevel;

    public RoleLevelUpEvent(int newLevel, int preLevel) {
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
