package com.stars.modules.daregod.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2017-08-24.
 */
public class DareGodEnterFightEvent extends Event {
    private int stageId;
    private int fightType;
    private int monsterId;

    public DareGodEnterFightEvent(int stageId, int fightType, int monsterId) {
        this.stageId = stageId;
        this.fightType = fightType;
        this.monsterId = monsterId;
    }

    public int getMonsterId() {
        return monsterId;
    }

    public int getStageId() {
        return stageId;
    }

    public int getFightType() {
        return fightType;
    }
}
