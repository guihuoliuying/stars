package com.stars.modules.newofflinepvp.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by chenkeyu on 2017-03-09 18:54
 */
public class RoleOfflinePvpPo extends DbRow {
    private long roleid;
    private int pvpCount;//战斗次数
    private int extraCount;//购买来的额外次数
    private int buyCount;//购买次数
    private int maxRank;//历史最大排名
    private int lastMaxRank;//上一次最大排名
    private byte first;//是否第一次打开//好恶习的功能(1:true,0:false)

    public int getLastMaxRank() {
        return lastMaxRank;
    }

    public void setLastMaxRank(int lastMaxRank) {
        this.lastMaxRank = lastMaxRank;
    }

    public long getRoleid() {
        return roleid;
    }

    public void setRoleid(long roleid) {
        this.roleid = roleid;
    }

    public int getPvpCount() {
        return pvpCount;
    }

    public void setPvpCount(int pvpCount) {
        this.pvpCount = pvpCount;
    }

    public int getBuyCount() {
        return buyCount;
    }

    public void setBuyCount(int buyCount) {
        this.buyCount = buyCount;
    }

    public int getMaxRank() {
        return maxRank;
    }

    public void setMaxRank(int maxRank) {
        this.maxRank = maxRank;
    }

    public int getExtraCount() {
        return extraCount;
    }

    public void setExtraCount(int extraCount) {
        this.extraCount = extraCount;
    }

    public byte getFirst() {
        return first;
    }

    public void setFirst(byte first) {
        this.first = first;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolenewofflinepvp", " `roleid`=" + this.roleid);
    }

    @Override
    public String getDeleteSql() {
        return "delete from rolenewofflinepvp where roleid=" + this.roleid;
    }
}
