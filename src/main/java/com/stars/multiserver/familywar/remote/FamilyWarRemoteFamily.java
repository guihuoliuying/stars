package com.stars.multiserver.familywar.remote;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2017-05-31.
 */
public class FamilyWarRemoteFamily extends DbRow {
    private long familyId;
    private int serverId;
    private String index;
    private int battleType;
    private int rank;

    private List<Integer> indexSet = new ArrayList<>();

    public FamilyWarRemoteFamily() {
    }

    public FamilyWarRemoteFamily(long familyId, int serverId) {
        this.familyId = familyId;
        this.serverId = serverId;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
        String[] tmp0 = index.split("\\+");
        for (String s : tmp0) {
            if (s == null || s.equals("")) continue;
            indexSet.add(Integer.parseInt(s));
        }
    }

    public List<Integer> getIndexSet() {
        return indexSet;
    }

    public void addIndex(int index) {
        if (!indexSet.contains(index)) {
            indexSet.add(index);
            String tmpStr = "";
            if (this.index == null || this.index.equals("")) {
                tmpStr = "";
            } else {
                tmpStr = this.index;
            }
            StringBuilder sb = new StringBuilder(tmpStr);
            if (sb.toString().equals("")) {
                sb.append(index);
            } else {
                sb.append("+").append(index);
            }
            this.index = sb.toString();
        }
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getBattleType() {
        return battleType;
    }

    public void setBattleType(int battleType) {
        this.battleType = battleType;
    }

    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_COMMON, "familywarremotefamily", " `familyid`=" + this.familyId + " and `serverId`=" + this.serverId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from familywarremotefamily where familyid=" + this.familyId + " and serverid=" + this.serverId;
    }
}
