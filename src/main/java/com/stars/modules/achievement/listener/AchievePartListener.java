package com.stars.modules.achievement.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.AchievementModule;
import com.stars.modules.newequipment.event.EquipStarAchieveEvent;
import com.stars.modules.newequipment.event.EquipStrengthAchieveEvent;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhouyaohui on 2016/12/20.
 */
public class AchievePartListener extends AbstractEventListener {
    public AchievePartListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        StringBuilder builder = new StringBuilder();
        Map<Byte, Integer> map = new HashMap<>();
        if (event instanceof EquipStarAchieveEvent) {
            builder.append(2).append("|");
            EquipStarAchieveEvent ese = (EquipStarAchieveEvent) event;
            map = ese.getStarLevelMap();
        }
        if (event instanceof EquipStrengthAchieveEvent) {
            builder.append(1).append("|");
            EquipStrengthAchieveEvent ese = (EquipStrengthAchieveEvent) event;
            map = ese.getStrengthLevelMap();
        }
        builder.append(StringUtil.makeString(map, '=', ','));
        AchievementModule am = (AchievementModule) module();
        am.triggerCheck(AchievementHandler.TYPE_PART, builder.toString());
    }
}
