package com.stars.modules.chargegift.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by chenxie on 2017/5/18.
 */
public class RoleChargeGiftPo extends DbRow {

    /**
     * 角色ID
     */
    private long roleId;

    /**
     * 当日已获得的礼包数量
     */
    private int giftNum;

    /**
     * 当日总充值额度
     */
    private int totalCharge;

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getGiftNum() {
        return giftNum;
    }

    public void setGiftNum(int giftNum) {
        this.giftNum = giftNum;
    }

    public int getTotalCharge() {
        return totalCharge;
    }

    public void setTotalCharge(int totalCharge) {
        this.totalCharge = totalCharge;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "chargegift", "`roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("chargegift", "`roleid`=" + roleId);
    }

}
