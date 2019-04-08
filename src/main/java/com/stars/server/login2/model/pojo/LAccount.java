package com.stars.server.login2.model.pojo;

import com.stars.server.login2.asyncdb.DbObjectState;
import com.stars.util.LogUtil;

import java.sql.Timestamp;

/**
 * Created by zhaowenshuo on 2016/2/1.
 */
public class LAccount {

    private String uniqueId; // format: channelId_userId

    private int channelId;
    private String account;
    private Timestamp regTimestamp; // 注册时间
    private Timestamp loginTimestamp; // 登录时间

    private com.stars.server.login2.asyncdb.DbObjectState state;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Timestamp getRegTimestamp() {
        return regTimestamp;
    }

    public void setRegTimestamp(Timestamp regTimestamp) {
        this.regTimestamp = regTimestamp;
    }

    public Timestamp getLoginTimestamp() {
        return loginTimestamp;
    }

    public void setLoginTimestamp(Timestamp loginTimestamp) {
        this.loginTimestamp = loginTimestamp;
    }

    public com.stars.server.login2.asyncdb.DbObjectState getState() {
        return state;
    }

    public void setState(DbObjectState state) {
    	LogUtil.info("Account State Changed[" + uniqueId +"]: " + this.state + " -> " + state);
        this.state = state;
    }

    public String toInsertSql() {
        return "insert into account (unique_id, channel_id, account, reg_timestamp, login_timestamp) " +
                "values ('" + uniqueId + "', " + channelId + ", '" + account + "', '" + regTimestamp.toString() +"', '" + loginTimestamp.toString() + "')";
    }

    public String toUpdateSqlWithRegTimestamp() {
        return "update account set reg_timestamp = '" + regTimestamp.toString() + "' where unique_id = '" + uniqueId + "'";
    }

    public String toUpdateSqlWithLoginTimestamp() {
        return "update account set login_timestamp = '" + loginTimestamp.toString() + "' where unique_id = '" + uniqueId + "'";
    }
}
