package com.stars.multiserver.familywar.data;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by chenkeyu on 2017-05-10 14:55
 */
public class FamilyWarRaceResult extends DbRow {
    private String battleId;
    private int battleType;
    private long camp1FamilyId;
    private long camp2FamilyId;
    private long winFamilyId;

    public String getBattleId() {
        return battleId;
    }

    public void setBattleId(String battleId) {
        this.battleId = battleId;
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

    public long getWinFamilyId() {
        return winFamilyId;
    }

    public void setWinFamilyId(long winFamilyId) {
        this.winFamilyId = winFamilyId;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_COMMON, "familywarraceresult", "battleid = " + this.battleId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from familywarraceresult where battleid = " + this.battleId;
    }
}
