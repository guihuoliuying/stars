package com.stars.modules.teamdungeon.event;

import com.stars.core.event.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * 组队副本结束事件
 * Created by liuyuheng on 2016/9/26.
 */
public class TeamDungeonFinishEvent extends Event {
    private int teamDungeonId;// 组队副本id
    private byte result;// 结果,胜利=2;失败=0
    private byte targetHpRemain;// 守护目标剩余血量百分比
    Map<String, Integer> damageMap = new HashMap<>();// 所有玩家造成的伤害统计 <unqueId, damage>
    private int spendTime;// 消耗时间
    private int stageId;

    public TeamDungeonFinishEvent(int teamDungeonId, byte result, byte targetHpRemain) {
        this.teamDungeonId = teamDungeonId;
        this.result = result;
        this.targetHpRemain = targetHpRemain;
    }

    public int getTeamDungeonId() {
        return teamDungeonId;
    }

    public void setTeamDungeonId(int teamDungeonId) {
        this.teamDungeonId = teamDungeonId;
    }

    public byte getResult() {
        return result;
    }

    public void setResult(byte result) {
        this.result = result;
    }

    public byte getTargetHpRemain() {
        return targetHpRemain;
    }

    public void setTargetHpRemain(byte targetHpRemain) {
        this.targetHpRemain = targetHpRemain;
    }

    public Map<String, Integer> getDamageMap() {
        return damageMap;
    }

    public void setDamageMap(Map<String, Integer> damageMap) {
        this.damageMap = damageMap;
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
}
