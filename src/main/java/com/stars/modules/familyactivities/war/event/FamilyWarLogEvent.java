package com.stars.modules.familyactivities.war.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-05-20.
 */
public class FamilyWarLogEvent extends Event {
    private int type;
    private int rank;
    private int kill;
    private long integral;
    private int success;
    private int warType;//1=本服家族战2=跨服家族战
    private int battleType;//1=小组赛2=决赛（单区家族战只有决赛）
    private Map<Integer, Integer> itemMap;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getKill() {
        return kill;
    }

    public void setKill(int kill) {
        this.kill = kill;
    }

    public long getIntegral() {
        return integral;
    }

    public void setIntegral(long integral) {
        this.integral = integral;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getWarType() {
        return warType;
    }

    public void setWarType(int warType) {
        this.warType = warType;
    }

    public int getBattleType() {
        return battleType;
    }

    public void setBattleType(int battleType) {
        this.battleType = battleType;
    }

    public Map<Integer, Integer> getItemMap() {
        return itemMap;
    }

    public void setItemMap(Map<Integer, Integer> itemMap) {
        this.itemMap = itemMap;
    }
}
