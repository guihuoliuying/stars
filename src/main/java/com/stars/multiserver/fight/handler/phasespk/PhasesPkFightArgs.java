package com.stars.multiserver.fight.handler.phasespk;

import com.stars.modules.scene.fightdata.FighterEntity;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/12/3.
 */
public class PhasesPkFightArgs {

    private int numOfFighter; // 人数
    private Map<Long, byte[]> enterPacketMap; // 发往客户端lua的进入包
    private Map<Long, FighterEntity> entityMap; // 发往服务端lua的进入包（玩家）
    private Map<Long, FighterEntity> buddyEntityMap; // 发往服务端lua的进入包（伙伴）
    private long timeLimitOfInitialPhase;
    private long timeLimitOfClientPreparationPhase;
    private Object args0;

    public PhasesPkFightArgs() {
    }

    public PhasesPkFightArgs(int numOfFighter, Map<Long, byte[]> enterPacketMap, Map<Long, FighterEntity> entityMap, long timeLimitOfInitialPhase, long timeLimitOfClientPreparationPhase, Object args0) {
        this.numOfFighter = numOfFighter;
        this.enterPacketMap = enterPacketMap;
        this.entityMap = entityMap;
        this.timeLimitOfInitialPhase = timeLimitOfInitialPhase;
        this.timeLimitOfClientPreparationPhase = timeLimitOfClientPreparationPhase;
        this.args0 = args0;
    }

    public int getNumOfFighter() {
        return numOfFighter;
    }

    public void setNumOfFighter(int numOfFighter) {
        this.numOfFighter = numOfFighter;
    }

    public Map<Long, byte[]> getEnterPacketMap() {
        return enterPacketMap;
    }

    public void setEnterPacketMap(Map<Long, byte[]> enterPacketMap) {
        this.enterPacketMap = enterPacketMap;
    }

    public Map<Long, FighterEntity> getEntityMap() {
        return entityMap;
    }

    public void setEntityMap(Map<Long, FighterEntity> entityMap) {
        this.entityMap = entityMap;
    }

    public Map<Long, FighterEntity> getBuddyEntityMap() {
        return buddyEntityMap;
    }

    public void setBuddyEntityMap(Map<Long, FighterEntity> buddyEntityMap) {
        this.buddyEntityMap = buddyEntityMap;
    }

    public long getTimeLimitOfInitialPhase() {
        return timeLimitOfInitialPhase;
    }

    public void setTimeLimitOfInitialPhase(long timeLimitOfInitialPhase) {
        this.timeLimitOfInitialPhase = timeLimitOfInitialPhase;
    }

    public long getTimeLimitOfClientPreparationPhase() {
        return timeLimitOfClientPreparationPhase;
    }

    public void setTimeLimitOfClientPreparationPhase(long timeLimitOfClientPreparationPhase) {
        this.timeLimitOfClientPreparationPhase = timeLimitOfClientPreparationPhase;
    }

    public Object getArgs0() {
        return args0;
    }

    public void setArgs0(Object args0) {
        this.args0 = args0;
    }
}
