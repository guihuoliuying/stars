package com.stars.modules.family.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/8.
 */
public class FamilySkillUpgradeEvent extends Event {

    private Map<String, Integer> skillLevelMap;

    public FamilySkillUpgradeEvent(Map<String, Integer> skillLevelMap) {
        this.skillLevelMap = skillLevelMap;
    }

    public Map<String, Integer> getSkillLevelMap() {
        return skillLevelMap;
    }

}
