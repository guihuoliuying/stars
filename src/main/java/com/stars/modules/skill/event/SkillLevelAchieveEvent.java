package com.stars.modules.skill.event;

import com.stars.core.event.Event;

/**
 * Created by zhanghaizhen on 2017/8/11.
 */
public class SkillLevelAchieveEvent extends Event {
    private int skillId;
    private int curLevel;

    public SkillLevelAchieveEvent(int skillId, int curLevel) {
        this.skillId = skillId;
        this.curLevel = curLevel;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public int getCurLevel() {
        return curLevel;
    }

    public void setCurLevel(int curLevel) {
        this.curLevel = curLevel;
    }
}
