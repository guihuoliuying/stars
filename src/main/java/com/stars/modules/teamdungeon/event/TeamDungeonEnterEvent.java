package com.stars.modules.teamdungeon.event;

import com.stars.core.event.Event;

/**
 * Created by liuyuheng on 2017/1/4.
 */
public class TeamDungeonEnterEvent extends Event {
    private int stageId;
    private int teamDungeonId;

    public TeamDungeonEnterEvent() {
    }

    public TeamDungeonEnterEvent(int stageId, int teamDungeonId) {
        this.stageId = stageId;
        this.teamDungeonId = teamDungeonId;
    }

    public int getStageId() {
        return stageId;
    }

    public void setStageId(int stageId) {
        this.stageId = stageId;
    }

    public int getTeamDungeonId() {
        return teamDungeonId;
    }

    public void setTeamDungeonId(int teamDungeonId) {
        this.teamDungeonId = teamDungeonId;
    }
}
