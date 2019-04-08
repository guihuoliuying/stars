package com.stars.services.family.activities.treasure.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by chenkeyu on 2017-03-15 16:40
 * 付费测内容
 */
public class RoleTreasureDamageRankPo extends DbRow implements Comparable<RoleTreasureDamageRankPo> {
    private long roleId;
    private long familyId;
    private int damage;
    private int fightScore;
    private int level;

    public RoleTreasureDamageRankPo() {
    }

    public RoleTreasureDamageRankPo(long roleId, long familyId, int damage, int fightScore, int level) {
        this.roleId = roleId;
        this.familyId = familyId;
        this.damage = damage;
        this.fightScore = fightScore;
        this.level = level;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getFightScore() {
        return fightScore;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roletreasuredamagerank", " `roleid`=" + this.roleId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from roletreasuredamagerank where roleid=" + this.roleId;
    }

    @Override
    public int compareTo(RoleTreasureDamageRankPo o) {
        return 0;
    }
}
