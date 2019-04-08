package com.stars.modules.familyactivities.treasure.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by chenkeyu on 2017/2/10 14:35
 */
public class RoleFamilyTreasure extends DbRow {
    private long roleId;                //
    private long damage;                //累积伤害
    private int adventureCount;         //每天探宝次数
    private int sundayAdventureCount;   //周日探宝次数
    private long lastResetDamage;        //上一次重置伤害的时间

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public long getDamage() {
        return damage;
    }

    public void setDamage(long damage) {
        this.damage = damage;
    }

    public void addDamage(long damageVal){
        this.damage = this.damage + damageVal;
    }

    public int getAdventureCount() {
        return adventureCount;
    }

    public void setAdventureCount(int adventureCount) {
        this.adventureCount = adventureCount;
    }

    public int getSundayAdventureCount() {
        return sundayAdventureCount;
    }

    public void setSundayAdventureCount(int sundayAdventureCount) {
        this.sundayAdventureCount = sundayAdventureCount;
    }

    public long getLastResetDamage() {
        return lastResetDamage;
    }

    public void setLastResetDamage(long lastResetDamage) {
        this.lastResetDamage = lastResetDamage;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER,"rolefamilytreasure","`roleid` = "+this.roleId);
    }

    @Override
    public String getDeleteSql() {
        return "";
    }
}
