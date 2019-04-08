package com.stars.modules.skill.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by chenkeyu on 2017/2/20 9:54
 */
public class SkillBatchLvUpEvent extends Event {
    private Map<Integer,Integer> skillLvMap;//skillId --level

    public SkillBatchLvUpEvent(Map<Integer, Integer> skillLvMap) {
        this.skillLvMap = skillLvMap;
    }

    public Map<Integer, Integer> getSkillLvMap() {
        return skillLvMap;
    }
}
