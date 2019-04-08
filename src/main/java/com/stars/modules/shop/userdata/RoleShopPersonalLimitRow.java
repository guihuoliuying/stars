package com.stars.modules.shop.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by zhouyaohui on 2016/9/7.
 */
public class RoleShopPersonalLimitRow extends DbRow {

    private long roleId;
    private int goodsId;
    private int times;

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(int goodsId) {
        this.goodsId = goodsId;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleshoppersonallimit",
                "roleid = " + roleId + " and goodsid = " + goodsId);
    }

    @Override
    public String getDeleteSql() {
        return "";
    }
}
