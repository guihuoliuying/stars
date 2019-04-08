package com.stars.modules.skill.event;

import com.stars.core.event.Event;

/**
 * Created by zhouyaohui on 2016/10/20.
 */
public class SkillLevelUpEvent extends Event {
    private int skillId;
    private int curLevel;

    public SkillLevelUpEvent(int skillId, int curLevel) {
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
