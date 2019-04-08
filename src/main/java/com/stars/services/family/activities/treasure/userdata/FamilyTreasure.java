package com.stars.services.family.activities.treasure.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by chenkeyu on 2017/2/11 14:12
 */
public class FamilyTreasure extends DbRow {
    private long familyId;          //家族Id
    private int level;              //level
    private int step;               //step
    private long damage;            //当前boss总伤害
    private long totalDamage;       //一周总伤害
    private long lastResetTimestamp;//上一次重置进度时间

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

    public void damageInc(long damage){
        this.damage = this.damage + damage;
    }

    public long getTotalDamage() {
        return totalDamage;
    }

    public void setTotalDamage(long totalDamage) {
        this.totalDamage = totalDamage;
    }

    public void totalDamageInc(long damages) {
        this.totalDamage = this.totalDamage + damages;
    }

    public long getLastResetTimestamp() {
        return lastResetTimestamp;
    }

    public void setLastResetTimestamp(long lastResetTimestamp) {
        this.lastResetTimestamp = lastResetTimestamp;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "familytreasure", " `familyid`=" + this.familyId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from familytreasure where `familyid`="+this.familyId;
    }
}
