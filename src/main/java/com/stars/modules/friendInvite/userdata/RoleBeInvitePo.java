package com.stars.modules.friendInvite.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by chenxie on 2017/6/10.
 */
public class RoleBeInvitePo extends DbRow {

    private long roleId;

    private String bindInviteCode;

    private byte status;

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getBindInviteCode() {
        return bindInviteCode;
    }

    public void setBindInviteCode(String bindInviteCode) {
        this.bindInviteCode = bindInviteCode;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }
    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_COMMON, "rolebeinvite", "`roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolebeinvite", "`roleid`=" + roleId);
    }

    @Override
    public String toString() {
        return "RoleBeInvitePo{" +
                "roleId=" + roleId +
                ", bindInviteCode='" + bindInviteCode + '\'' +
                ", status=" + status +
                '}';
    }
}
