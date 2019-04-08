package com.stars.modules.teamdungeon.event;

import com.stars.core.event.Event;

/**
 * Created by liuyuheng on 2017/1/4.
 */
public class TeamDungeonExitEvent extends Event {
    private int teamDungeonId;
    private int spendTime;// 消耗时间
    private int stageId;

    public TeamDungeonExitEvent() {
    }

    public TeamDungeonExitEvent(int teamDungeonId, int spendTime, int stageId) {
        this.teamDungeonId = teamDungeonId;
        this.spendTime = spendTime;
        this.stageId = stageId;
    }

    public int getSpendTime() {
        return spendTime;
    }

    public void setSpendTime(int spendTime) {
        this.spendTime = spendTime;
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
