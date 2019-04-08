package com.stars.multiserver.fightutil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-05-04 18:31
 */
public class FightArgs {
    private String battleId;
    private Map<Long, Byte> campMap = new HashMap<>();
    private int camp1MainServerId;
    private int camp2MainServerId;
    private long createTimestamp;

    public String getBattleId() {
        return battleId;
    }

    public void setBattleId(String battleId) {
        this.battleId = battleId;
    }

    public Map<Long, Byte> getCampMap() {
        return campMap;
    }

    public void setCampMap(Map<Long, Byte> campMap) {
        this.campMap = campMap;
    }

    public int getCamp1MainServerId() {
        return camp1MainServerId;
    }

    public void setCamp1MainServerId(int camp1MainServerId) {
        this.camp1MainServerId = camp1MainServerId;
    }

    public int getCamp2MainServerId() {
        return camp2MainServerId;
    }

    public void setCamp2MainServerId(int camp2MainServerId) {
        this.camp2MainServerId = camp2MainServerId;
    }

    public long getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }
}
