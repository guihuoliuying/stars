package com.stars.modules.soul.event;

import com.stars.modules.soul.prodata.SoulLevel;

import java.util.Map;

/**
 * Created by huwenjun on 2017/11/22.
 */
public class SoulStageUpEvent extends SoulLevelUpEvent {
    public SoulStageUpEvent(int stage, int type, int level,Map<Integer, SoulLevel> soulLevelsMap) {
        super(stage, type, level,soulLevelsMap);
    }
}
