package com.stars.modules.teamdungeon.event;

import com.stars.core.event.Event;

/**
 * 在副本中死亡且无复活次数
 * Created by gaopeidian on 2016/11/8.
 */
public class DeadInTeamDungeonEvent extends Event {
    private int teamDungeonId;
    private int damage;
    private int stageId;
    private int spendTime;

    public DeadInTeamDungeonEvent(int teamDungeonId, int damage) {
        this.teamDungeonId = teamDungeonId;
        this.damage = damage;
    }

    public int getTeamDungeonId() {
        return teamDungeonId;
    }
    
    public int getDamage() {
        return damage;
    }

    public int getStageId() {
        return stageId;
    }

    public void setStageId(int stageId) {
        this.stageId = stageId;
    }

    public int getSpendTime() {
        return spendTime;
    }

    public void setSpendTime(int spendTime) {
        this.spendTime = spendTime;
    }
}
