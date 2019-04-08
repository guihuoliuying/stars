package com.stars.modules.luckycard.usrdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by huwenjun on 2017/9/27.
 */
public class RoleLuckyCardTime extends DbRow {
    private long roleId;
    private long currentEndTime;
    private int totalPayCount;//累计充值额度,兑完奖减少

    public RoleLuckyCardTime(long roleId, long currentEndTime) {
        this.roleId = roleId;
        this.currentEndTime = currentEndTime;
    }

    public RoleLuckyCardTime() {
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleluckycardtime", " roleid=" + roleId);

    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("roleluckycardtime", " roleid=" + roleId);
    }

    public long getCurrentEndTime() {
        return currentEndTime;
    }

    public void setCurrentEndTime(long currentEndTime) {
        this.currentEndTime = currentEndTime;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getTotalPayCount() {
        return totalPayCount;
    }

    public void setTotalPayCount(int totalPayCount) {
        this.totalPayCount = totalPayCount;
    }

    public void addTotalPayCount(int payCount) {
        this.totalPayCount += payCount;
    }
}
