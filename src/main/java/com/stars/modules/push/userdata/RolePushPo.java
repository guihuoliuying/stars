package com.stars.modules.push.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.modules.push.PushManager;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class RolePushPo extends DbRow {

    private long roleId;
    private int pushId;
    private int state; // 推送状态
    private int numberOfTimes; // 已推送次数

    public RolePushPo() {
    }

    public RolePushPo(long roleId, int pushId) {
        this.roleId = roleId;
        this.pushId = pushId;
        this.state = PushManager.STATE_INACTIVED;
        this.numberOfTimes = 0;
    }

    @Override
    public String toString() {
        return pushId + ":" + state;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolepush", "`roleid`=" + roleId + " and `pushid`=" + pushId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `rolepush` where `roleid`=" + roleId + " and `pushid`=" + pushId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getPushId() {
        return pushId;
    }

    public void setPushId(int pushId) {
        this.pushId = pushId;
    }

    public int getNumberOfTimes() {
        return numberOfTimes;
    }

    public void setNumberOfTimes(int times) {
        this.numberOfTimes = times;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean isActived() {
        return state == PushManager.STATE_ACTIVED;
    }

    public boolean isInactived() {
        return state == PushManager.STATE_INACTIVED;
    }
}
