package com.stars.services.friend.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by zhaowenshuo on 2016/8/10.
 */
public class BlackerPo extends DbRow {

    private long roleId;
    private long blackerId;
    private String blackerName;

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public long getBlackerId() {
        return blackerId;
    }

    public void setBlackerId(long blackerId) {
        this.blackerId = blackerId;
    }

    public String getBlackerName() {
        return blackerName;
    }

    public void setBlackerName(String blackerName) {
        this.blackerName = blackerName;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "friendblacker", "`roleid`=" + roleId + " and `blackerid`=" + blackerId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `friendblacker` where `roleid`=" + roleId + " and `blackerid`=" + blackerId;
    }
}
