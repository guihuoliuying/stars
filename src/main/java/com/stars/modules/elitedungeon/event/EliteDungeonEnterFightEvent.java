package com.stars.modules.elitedungeon.event;

import com.stars.core.event.Event;

/**
 * Created by gaopeidian on 2017/3/10.
 */
public class EliteDungeonEnterFightEvent extends Event {
    private int stageId;
    private int eliteDungeonId;

    public EliteDungeonEnterFightEvent() {
    }

    public EliteDungeonEnterFightEvent(int stageId, int eliteDungeonId) {
        this.stageId = stageId;
        this.eliteDungeonId = eliteDungeonId;
    }

    public int getStageId() {
        return stageId;
    }

    public void setStageId(int stageId) {
        this.stageId = stageId;
    }

    public int getEliteDungeonId() {
        return eliteDungeonId;
    }

    public void setEliteDungeonId(int eliteDungeonId) {
        this.eliteDungeonId = eliteDungeonId;
    }
}
