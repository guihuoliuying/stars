package com.stars.modules.elitedungeon.event;

import com.stars.core.event.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * 组队精英副本结束事件
 * Created by gaopeidian on 2017/4/7.
 */
public class EliteDungeonFinishEvent extends Event {
    private int eliteDungeonId;// 副本id
    private byte result;// 结果,胜利=2;失败=0
    Map<String, Integer> damageMap = new HashMap<>();// 所有玩家造成的伤害统计 <unqueId, damage>
    private int spendTime;// 消耗时间
    private int stageId;

    public EliteDungeonFinishEvent(int eliteDungeonId, byte result) {
        this.eliteDungeonId = eliteDungeonId;
        this.result = result;
    }

    public int getEliteDungeonId() {
        return eliteDungeonId;
    }

    public void setEliteDungeonId(int eliteDungeonId) {
        this.eliteDungeonId = eliteDungeonId;
    }

    public byte getResult() {
        return result;
    }

    public void setResult(byte result) {
        this.result = result;
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
