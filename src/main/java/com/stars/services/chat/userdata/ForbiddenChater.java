package com.stars.services.chat.userdata;import com.stars.bootstrap.ServerManager;import com.stars.core.db.DBUtil;import com.stars.core.db.DbRow;import com.stars.core.db.SqlUtil;/** * Created by liuyuheng on 2017/1/16. */public class ForbiddenChater extends DbRow {    private long roleId;// '角色Id'    private int serverId;// '服务器Id'    private long startTime;// '开始禁言时间戳(毫秒)'    private long expireTime;// '禁言结束时间戳(毫秒)'    private String reason;// '禁言原因'    public ForbiddenChater() {    }    public ForbiddenChater(long roleId, long startTime, long expireTime, String reason) {        this.roleId = roleId;        this.serverId = ServerManager.getServer().getConfig().getServerId();        this.startTime = startTime;        this.expireTime = expireTime;        this.reason = reason;    }    @Override    public String getChangeSql() {        return SqlUtil.getSql(this, DBUtil.DB_USER, "forbiddenchater", " `serverid`=" + serverId + " and `roleid`=" + roleId);    }    @Override    public String getDeleteSql() {        return SqlUtil.getDeleteSql("forbiddenchater", " `serverid`=" + serverId + " and `roleid`=" + roleId);    }    public long getRoleId() {        return roleId;    }    public void setRoleId(long roleId) {        this.roleId = roleId;    }    public int getServerId() {        return serverId;    }    public void setServerId(int serverId) {        this.serverId = serverId;    }    public long getStartTime() {        return startTime;    }    public void setStartTime(long startTime) {        this.startTime = startTime;    }    public long getExpireTime() {        return expireTime;    }    public void setExpireTime(long expireTime) {        this.expireTime = expireTime;    }    public String getReason() {        return reason;    }    public void setReason(String reason) {        this.reason = reason;    }}