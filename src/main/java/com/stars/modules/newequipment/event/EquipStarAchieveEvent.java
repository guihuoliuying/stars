package com.stars.modules.newequipment.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/8/11.
 */
public class EquipStarAchieveEvent extends Event {
    private int totalStarLevel;
    private Map<Byte,Integer> starLevelMap;

    public EquipStarAchieveEvent(int totalStarLevel,Map<Byte,Integer> starLevelMap) {
        this.totalStarLevel = totalStarLevel;
        this.starLevelMap = starLevelMap;
    }

    public int getTotalStarLevel() {
        return totalStarLevel;
    }

    public Map<Byte, Integer> getStarLevelMap() {
        return starLevelMap;
    }
}
