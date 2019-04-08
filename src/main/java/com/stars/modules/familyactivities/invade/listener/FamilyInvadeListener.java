package com.stars.modules.familyactivities.invade.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.buddy.event.FightBuddyChangeEvent;
import com.stars.modules.familyactivities.invade.FamilyInvadeModule;
import com.stars.modules.familyactivities.invade.event.*;
import com.stars.modules.role.event.FightScoreChangeEvent;
import com.stars.modules.skill.event.SkillPositionChangeEvent;

/**
 * Created by liuyuheng on 2016/10/21.
 */
public class FamilyInvadeListener extends AbstractEventListener<FamilyInvadeModule> {
    public FamilyInvadeListener(FamilyInvadeModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof FamilyInvadeDungeonDropEvent) {
            FamilyInvadeDungeonDropEvent fiddEvent = (FamilyInvadeDungeonDropEvent) event;
            module().addMonsterDrop(fiddEvent.getDropIds());
        }
        if (event instanceof FamilyInvadeDungeonFinishEvent) {
            FamilyInvadeDungeonFinishEvent fidfEvent = (FamilyInvadeDungeonFinishEvent) event;
            module().finishReward(fidfEvent.getResult(), fidfEvent.getInvadeId());
        }
        if (event instanceof FamilyInvadeAwardBoxEvent) {
            FamilyInvadeAwardBoxEvent fiabEvent = (FamilyInvadeAwardBoxEvent) event;
            module().spawnAwardBox(fiabEvent.getBoxMap());
        }
        if (event instanceof FamilyActInvadeStartEvent) {
            module().invadeStartHandler();
        }
        if (event instanceof FamilyInvadeEnterDungeonEvent) {
            module().enterInvadeDungeonHandler(event);
        }
        // 玩家/宠物数据改变,更新到家族入侵
        if (event instanceof FightScoreChangeEvent || event instanceof SkillPositionChangeEvent
                || event instanceof FightBuddyChangeEvent) {
            module().updateMember();
        }
    }
}
