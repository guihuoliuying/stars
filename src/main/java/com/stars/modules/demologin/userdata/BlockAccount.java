package com.stars.modules.demologin.userdata;

import com.stars.bootstrap.ServerManager;
import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by liuyuheng on 2017/1/16.
 */
public class BlockAccount extends DbRow {
    private String account;// '账号'
    private int serverId;// '服务器Id'
    private long startTime;// '封号开始时间戳(毫秒)'
    private long expireTime;// '封号结束时间戳(毫秒)'
    private String reason;// '封号原因'

    public BlockAccount() {
    }

    public BlockAccount(String account, long startTime, long expireTime, String reason) {
        this.account = account;
        this.startTime = startTime;
        this.serverId = ServerManager.getServer().getConfig().getServerId();
        this.expireTime = expireTime;
        this.reason = reason;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "blockaccount",
                " `account`='" + account + "' and `serverid`=" + serverId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("blockaccount",
                " `account`='" + account + "' and `serverid`=" + serverId);
    }
}
