package com.stars.multiserver.familywar.qualifying.cache;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by chenkeyu on 2017-05-19.
 */
public class FamilyWarQualifyingFixtureCache extends DbRow {
    private int battleType;
    private int groupId;
    private long camp1FamilyId;
    private long camp2FamilyId;
    private int camp1ServerId;
    private int camp2ServerId;
    private long markfinish;
    private long winnerFamilyId;

    public int getCamp1ServerId() {
        return camp1ServerId;
    }

    public void setCamp1ServerId(int camp1ServerId) {
        this.camp1ServerId = camp1ServerId;
    }

    public int getCamp2ServerId() {
        return camp2ServerId;
    }

    public void setCamp2ServerId(int camp2ServerId) {
        this.camp2ServerId = camp2ServerId;
    }

    public long getMarkfinish() {
        return markfinish;
    }

    public void setMarkfinish(long markfinish) {
        this.markfinish = markfinish;
    }

    public int getBattleType() {
        return battleType;
    }

    public void setBattleType(int battleType) {
        this.battleType = battleType;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public long getCamp1FamilyId() {
        return camp1FamilyId;
    }

    public void setCamp1FamilyId(long camp1FamilyId) {
        this.camp1FamilyId = camp1FamilyId;
    }

    public long getCamp2FamilyId() {
        return camp2FamilyId;
    }

    public void setCamp2FamilyId(long camp2FamilyId) {
        this.camp2FamilyId = camp2FamilyId;
    }

    public long getWinnerFamilyId() {
        return winnerFamilyId;
    }

    public void setWinnerFamilyId(long winnerFamilyId) {
        this.winnerFamilyId = winnerFamilyId;
    }

    public boolean contain(long familyId) {
        return camp1FamilyId == familyId || camp2FamilyId == familyId;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_COMMON,
                "qualifyingfixture", "`battletype`=" + this.battleType + " and " + "`groupid`=" +
                        this.groupId + " and " + "`camp1familyid`=" + this.camp1FamilyId + " and " + "`camp2familyid`=" +
                        this.camp2FamilyId + " and " + "`markfinish`=" + this.markfinish);
    }

    @Override
    public String getDeleteSql() {
        return "delete from qualifyingfixture where `battletype`=" + this.battleType + " and `groupid`=" +
                this.groupId + " and `camp1familyid`=" + this.camp1FamilyId + " and `camp2familyid`=" +
                this.camp2FamilyId + " and `markfinish`=" + this.markfinish;
    }
}
