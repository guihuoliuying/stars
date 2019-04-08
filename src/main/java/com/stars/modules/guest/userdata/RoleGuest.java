package com.stars.modules.guest.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by zhouyaohui on 2017/1/3.
 */
public class RoleGuest extends DbRow {
    private long roleId;
    private int guestId;
    private int level;

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleguest", "roleid = " + roleId + " and guestid = " + guestId);
    }

    @Override
    public String getDeleteSql() {
        return "";
    }
}
