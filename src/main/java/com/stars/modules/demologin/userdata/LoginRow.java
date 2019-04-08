package com.stars.modules.demologin.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;

/**
 * Created by zhaowenshuo on 2015/12/30.
 */
public class LoginRow extends DbRow implements Comparable {

    private long roleId;
    private String roleName;
    private long lastLoginTimestamp;
    private long lastDailyResetTimestamp;
    private long lastWeeklyResetTimestamp;
    private long lastMonthlyResetTimestamp;
    private long lastFiveOClockResetTimestamp;

    private int creationState;
    private long offlineTime;

    // 内存数据
    private long lastLastLoginTimestamp;

    public LoginRow() {
    }

    public LoginRow(long roleId) {
        this.roleId = roleId;
        this.roleName = "";
        this.creationState = 1;
//        setInsertStatus();
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof LoginRow){
            LoginRow tmpVo = (LoginRow)o;
            if(tmpVo.lastLoginTimestamp > this.lastLoginTimestamp){
                return 1;
            }else if(tmpVo.lastLoginTimestamp == this.lastLoginTimestamp){
                return  0;
            }
        }
        return  -1;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "login", " roleid=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("login", " roleid=" + roleId);
    }
    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
        setUpdateStatus();
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
        setUpdateStatus();
    }

    public long getLastLoginTimestamp() {
        return lastLoginTimestamp;
    }

    public void setLastLoginTimestamp(long lastLoginTimestamp) {
        this.lastLastLoginTimestamp = this.lastLoginTimestamp;
        this.lastLoginTimestamp = lastLoginTimestamp;
        setUpdateStatus();
    }

    public long getLastDailyResetTimestamp() {
        return lastDailyResetTimestamp;
    }

    public void setLastDailyResetTimestamp(long lastDailyResetTimestamp) {
        this.lastDailyResetTimestamp = lastDailyResetTimestamp;
    }

    public long getLastFiveOClockResetTimestamp() {
        return lastFiveOClockResetTimestamp;
    }

    public void setLastFiveOClockResetTimestamp(long lastFiveOClockResetTimestamp) {
        this.lastFiveOClockResetTimestamp = lastFiveOClockResetTimestamp;
    }

    public long getLastWeeklyResetTimestamp() {
        return lastWeeklyResetTimestamp;
    }

    public void setLastWeeklyResetTimestamp(long lastWeeklyResetTimestamp) {
        this.lastWeeklyResetTimestamp = lastWeeklyResetTimestamp;
    }

    public long getLastMonthlyResetTimestamp() {
        return lastMonthlyResetTimestamp;
    }

    public void setLastMonthlyResetTimestamp(long lastMonthlyResetTimestamp) {
        this.lastMonthlyResetTimestamp = lastMonthlyResetTimestamp;
    }

    public int getCreationState() {
        return creationState;
    }

    public void setCreationState(int creationState) {
        this.creationState = creationState;
        setUpdateStatus();
    }

    public long getOfflineTime() {
		return offlineTime;
	}

	public void setOfflineTime(long offlineTime) {
		this.offlineTime = offlineTime;
	}

	public boolean isCreated() {
        return creationState == 1;
    }

    public long getLastLastLoginTimestamp() {
        return lastLastLoginTimestamp;
    }

    public void setLastLastLoginTimestamp(long lastLastLoginTimestamp) {
        this.lastLastLoginTimestamp = lastLastLoginTimestamp;
    }
}
