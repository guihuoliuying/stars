package com.stars.services.offlinepvp.cache;

import com.stars.modules.scene.fightdata.FighterEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * 离线pvp匹配对手缓存,可能是玩家,可能是机器人
 * Created by liuyuheng on 2016/10/10.
 */
public class OPEnemyCache {
    private String uniqueId;// 玩家=roleId;机器人="r"+robotId
    private int jobId;// 职业
    private Map<String, FighterEntity> entityMap = new HashMap<>();

    public OPEnemyCache() {
    }

    public OPEnemyCache(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public int getRoleLevel() {
        if (!entityMap.containsKey(uniqueId))
            throw new IllegalArgumentException("");
        return entityMap.get(uniqueId).getLevel();
    }

    public int getFightScore() {
        return entityMap.get(uniqueId).getFightScore();
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return entityMap.get(uniqueId).getName();
    }

    public int getModelId() {
        return entityMap.get(uniqueId).getModelId();
    }

    public Map<String, FighterEntity> getEntityMap() {
        return entityMap;
    }

    public void setEntityMap(Map<String, FighterEntity> entityMap) {
        this.entityMap = entityMap;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getCurDeityWeapon() {
        return entityMap.get(uniqueId).getCurDeityWeapon();
    }
}
