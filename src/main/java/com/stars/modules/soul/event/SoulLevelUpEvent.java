package com.stars.modules.soul.event;

import com.stars.core.event.Event;
import com.stars.modules.soul.prodata.SoulLevel;

import java.util.Map;

/**
 * Created by huwenjun on 2017/11/22.
 */
public class SoulLevelUpEvent extends Event {
    private int stage;//阶级
    private int type;//位置
    private int level;//等级
    private Map<Integer, SoulLevel> soulLevelsMap;

    public SoulLevelUpEvent() {
    }

    public SoulLevelUpEvent(int stage, int type, int level, Map<Integer, SoulLevel> soulLevelsMap) {
        this.stage = stage;
        this.type = type;
        this.level = level;
        this.soulLevelsMap = soulLevelsMap;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Map<Integer, SoulLevel> getSoulLevelsMap() {
        return soulLevelsMap;
    }

    public void setSoulLevelsMap(Map<Integer, SoulLevel> soulLevelsMap) {
        this.soulLevelsMap = soulLevelsMap;
    }
}
