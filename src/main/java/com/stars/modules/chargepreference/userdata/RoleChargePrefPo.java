package com.stars.modules.chargepreference.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by zhaowenshuo on 2017/3/29.
 */
public class RoleChargePrefPo extends DbRow {

    private long roleId;
    private int prefId;
    private byte isRebate; // 是否打折
//    private byte isChosen; // 是否选中
    private int chargeNumber; // 充值金额

    public RoleChargePrefPo() {
    }

    public RoleChargePrefPo(long roleId, int prefId) {
        this.roleId = roleId;
        this.prefId = prefId;
        this.chargeNumber = 0;
        setRebate(false);
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolechargepreference", "`roleid`=" + roleId + " and `prefid`=" + prefId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `rolechargepreference` where `roleid`=" + roleId + " and `prefid`=" + prefId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getPrefId() {
        return prefId;
    }

    public void setPrefId(int prefId) {
        this.prefId = prefId;
    }

    public byte getIsRebate() {
        return isRebate;
    }

    public void setIsRebate(byte isRebate) {
        this.isRebate = isRebate;
    }

    public int getChargeNumber() {
        return chargeNumber;
    }

    public void setChargeNumber(int chargeNumber) {
        this.chargeNumber = chargeNumber;
    }

    public void setRebate(boolean isRebate) {
        this.isRebate = (byte) (isRebate ? 1 : 0);
    }

    public boolean isRebate() {
        return this.isRebate == 1;
    }

    @Override
    public String toString() {
        return "(" + prefId + "=" + chargeNumber + ")";
    }
}
