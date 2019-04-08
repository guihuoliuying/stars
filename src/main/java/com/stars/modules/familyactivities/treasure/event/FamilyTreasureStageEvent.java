package com.stars.modules.familyactivities.treasure.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2017/2/13 11:48
 */
public class FamilyTreasureStageEvent extends Event {
    private int level;//家族探宝阶级
    private int step;//家族探宝步数
    private long damage;     //当前boss被伤害值
    private int startType;//当前活动开始的类型

    private long totalDamage;//一周内所有boss被伤害值
    private int rank;//当前家族探宝的排名

    private boolean flushToClient;
    private boolean resetDamage;

    public FamilyTreasureStageEvent() {
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public long getDamage() {
        return damage;
    }

    public void setDamage(long damage) {
        this.damage = damage;
    }

    public int getStartType() {
        return startType;
    }

    public long getTotalDamage() {
        return totalDamage;
    }

    public void setTotalDamage(long totalDamage) {
        this.totalDamage = totalDamage;
    }

    public int getRank() {
        return rank;
    }

    public boolean isFlushToClient() {
        return flushToClient;
    }

    public void setFlushToClient(boolean flushToClient) {
        this.flushToClient = flushToClient;
    }

    public void setStartType(int startType) {
        this.startType = startType;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public boolean isResetDamage() {
        return resetDamage;
    }

    public void setResetDamage(boolean resetDamage) {
        this.resetDamage = resetDamage;
    }
}
