package com.stars.modules.task.listener;


import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.authentic.event.AuthenticEvent;
import com.stars.modules.buddy.event.BuddyUpgradeEvent;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.newequipment.event.EquipStrengthChangeEvent;
import com.stars.modules.newequipment.event.EquipWashChangeEvent;
import com.stars.modules.newofflinepvp.NewOfflinePvpManager;
import com.stars.modules.newofflinepvp.event.OfflinePvpClientStageFinishEvent;
import com.stars.modules.ride.event.RideLevelUpEvent;
import com.stars.modules.soul.event.SoulLevelUpEvent;
import com.stars.modules.soul.prodata.SoulLevel;
import com.stars.modules.task.TaskManager;
import com.stars.modules.task.TaskModule;

import java.util.Map;

/**
 * Created by chenkeyu on 2017/1/7 15:24
 */
public class BranchTaskListener implements EventListener {
    private TaskModule module;

    public BranchTaskListener(TaskModule module) {
        this.module = module;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof DailyFuntionEvent) {//
            int dailyId = ((DailyFuntionEvent) event).getDailyId();
            module.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_DailyCount, String.valueOf(dailyId)), 1, false);
        }
        if (event instanceof OfflinePvpClientStageFinishEvent) {
            if (((OfflinePvpClientStageFinishEvent) event).getFinish() == NewOfflinePvpManager.victory) {
                module.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_PvpCount, "time"), 1, false);
            }
        }
        if (event instanceof EquipWashChangeEvent) {
            module.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_EquipWashCount, "time"), 1, false);
        }
        if (event instanceof RideLevelUpEvent) {
            int maxLevel = ((RideLevelUpEvent) event).getCurrLevelId();
            module.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_RideUpLv, "lv"), maxLevel, true);
        }
        if (event instanceof BuddyUpgradeEvent) {
            int maxLevel = 0;
            for (int level : ((BuddyUpgradeEvent) event).getRoleBuddyLevelMap().values()) {
                maxLevel = level > maxLevel ? level : maxLevel;
            }
            module.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_BuddyUpLv, "lv"), maxLevel, true);
        }
        if (event instanceof EquipStrengthChangeEvent) {
            int maxLevel = 0;
            for (int level : ((EquipStrengthChangeEvent) event).getStrengthLevelMap().values()) {
                maxLevel = level > maxLevel ? level : maxLevel;
            }
            module.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_EquipStrengthLv, "lv"), maxLevel, true);
        }
        if (event instanceof AuthenticEvent) {
            module.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_Authentic, "time"),
                    ((AuthenticEvent) event).getTimes(), false);
        }
        if (event instanceof SoulLevelUpEvent) {
            SoulLevelUpEvent soulLevelUpEvent = (SoulLevelUpEvent) event;
            Map<Integer, SoulLevel> soulLevelsMap = soulLevelUpEvent.getSoulLevelsMap();
            for(SoulLevel soulLevel:soulLevelsMap.values()){
                module.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_SoulLevel, soulLevel.getSoulGodType() + ""),
                        soulLevel.getSoulGodLevel(), true);
            }
            module.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_SoulStage,  "2"),
                    soulLevelUpEvent.getStage(), true);
        }
    }
}
