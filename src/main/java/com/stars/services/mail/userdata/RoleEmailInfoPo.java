package com.stars.services.mail.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by zhaowenshuo on 2016/8/2.
 */
public class RoleEmailInfoPo extends DbRow {

    private long roleId;
    private int roleEmailId;
    private int allEmailId;

    public RoleEmailInfoPo() {
    }

    public RoleEmailInfoPo(long roleId, int roleEmailId, int allEmailId) {
        this.roleId = roleId;
        this.roleEmailId = roleEmailId;
        this.allEmailId = allEmailId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getRoleEmailId() {
        return roleEmailId;
    }

    public void setRoleEmailId(int roleEmailId) {
        this.roleEmailId = roleEmailId;
    }

    public int getAllEmailId() {
        return allEmailId;
    }

    public void setAllEmailId(int allEmailId) {
        this.allEmailId = allEmailId;
    }

    public int nextRoleEmailId() {
        return ++roleEmailId;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "`roleemailinfo`", "`roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return null;
    }

    @Override
    public String toString() {
        return "RoleEmailInfoPo{" +
                "roleId=" + roleId +
                ", roleEmailId=" + roleEmailId +
                ", allEmailId=" + allEmailId +
                '}';
    }
}
