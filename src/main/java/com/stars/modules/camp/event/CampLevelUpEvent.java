package com.stars.modules.camp.event;

import com.stars.core.event.Event;

/**
 * Created by huwenjun on 2017/6/29.
 */
public class CampLevelUpEvent extends Event {
    private int campType;
    private int newLevel;

    public CampLevelUpEvent(int campType, int newLevel) {
        this.campType = campType;
        this.newLevel = newLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public void setNewLevel(int newLevel) {
        this.newLevel = newLevel;
    }

    public int getCampType() {
        return campType;
    }

    public void setCampType(int campType) {
        this.campType = campType;
    }

}
