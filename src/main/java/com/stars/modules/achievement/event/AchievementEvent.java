package com.stars.modules.achievement.event;

import com.stars.core.event.Event;

/**
 * Created by zhouyaohui on 2016/12/7.
 */
public class AchievementEvent extends Event {
    private int achieveId;

    public AchievementEvent(int id) {
        achieveId = id;
    }

    public int getAchieveId() {
        return achieveId;
    }
}
