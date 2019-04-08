package com.stars.modules.friendInvite.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by chenxie on 2017/6/9.
 */
public class RoleInvitePo extends DbRow {

    private long roleId;

    private String inviteCode;

    private int inviteCount;

    private int fetchCount;

    private int serverId;

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public int getInviteCount() {
        return inviteCount;
    }

    public void setInviteCount(int inviteCount) {
        this.inviteCount = inviteCount;
    }

    public int getFetchCount() {
        return fetchCount;
    }

    public void setFetchCount(int fetchCount) {
        this.fetchCount = fetchCount;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_COMMON, "roleinvite", "`roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("roleinvite", "`roleid`=" + roleId);
    }

    @Override
    public String toString() {
        return "RoleInvitePo{" +
                "roleId=" + roleId +
                ", inviteCode='" + inviteCode + '\'' +
                ", inviteCount=" + inviteCount +
                ", fetchCount=" + fetchCount +
                ", serverId=" + serverId +
                '}';
    }
}
