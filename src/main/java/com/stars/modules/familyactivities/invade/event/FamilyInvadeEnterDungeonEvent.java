package com.stars.modules.familyactivities.invade.event;

import com.stars.core.event.Event;

/**
 * Created by liuyuheng on 2016/10/26.
 */
public class FamilyInvadeEnterDungeonEvent extends Event {
    private int stageId;

    public FamilyInvadeEnterDungeonEvent(int stageId) {
        this.stageId = stageId;
    }

    public int getStageId() {
        return stageId;
    }
}
