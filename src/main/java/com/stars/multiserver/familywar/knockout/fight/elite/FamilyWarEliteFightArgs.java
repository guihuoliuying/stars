package com.stars.multiserver.familywar.knockout.fight.elite;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/12/7.
 */
public class FamilyWarEliteFightArgs {

    private String battleId;
    private Map<Long, Byte> campMap = new HashMap<>();
    private int camp1MainServerId;
    private int camp2MainServerId;
    private long createTimestamp;
    private Map<Long, Integer> roleWarType = new HashMap<>();

    public Map<Long, Integer> getRoleWarType() {
        return roleWarType;
    }

    public void setRoleWarType(Map<Long, Integer> roleWarType) {
        this.roleWarType = roleWarType;
    }

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
