package com.stars.services.family.activities.bonfire.cache;

import com.stars.modules.familyactivities.bonfire.FamilyBonfrieManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wuyuxing on 2017/3/9.
 */
public class FamilyBonFireData {

    private long familyId;
    private int level;
    private long exp;
    private long lastUpdateExpTimes;//上次更新扣除经验时间

    private Map<Long,RoleBonFireCache> roleBonFireMap;
    private Map<Long,RoleQuestionCache> roleQuestionMap;

    public FamilyBonFireData() {
    }

    public FamilyBonFireData(long familyId) {
        this.familyId = familyId;
        this.level = FamilyBonfrieManager.DEFAULT_LEVEL;
        this.exp = FamilyBonfrieManager.DEFAULT_EXP;
        this.roleBonFireMap = new HashMap<>();
        this.roleQuestionMap = new HashMap<>();
    }

    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public long getLastUpdateExpTimes() {
        return lastUpdateExpTimes;
    }

    public void setLastUpdateExpTimes(long lastUpdateExpTimes) {
        this.lastUpdateExpTimes = lastUpdateExpTimes;
    }

    public void reduceExp(int exp){
        if(exp <= 0) return;
        this.exp -= exp;
    }

    public void addExp(int exp){
        this.exp += exp;
    }

    public void reduceLevel(){
        this.level--;
    }

    public Map<Long, RoleBonFireCache> getRoleBonFireMap() {
        return roleBonFireMap;
    }

    public Map<Long, RoleQuestionCache> getRoleQuestionMap() {
        return roleQuestionMap;
    }

    public void recordAnswer(long roleId,String name,int questionId,boolean isRight){
        RoleQuestionCache cache = roleQuestionMap.get(roleId);
        if(cache == null){
            cache = new RoleQuestionCache(familyId,roleId,name);
            roleQuestionMap.put(roleId,cache);
        }
        cache.addAnswerQuestionId(questionId);
        if(isRight){
            cache.addRightCount();
        }
    }
}
