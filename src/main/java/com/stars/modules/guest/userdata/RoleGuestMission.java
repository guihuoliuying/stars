package com.stars.modules.guest.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by zhouyaohui on 2017/1/6.
 */
public class RoleGuestMission extends DbRow {
    public final static byte UNDISPATCH = 0;    // 未派遣
    public final static byte ONMISSION = 1;     // 执行中
    public final static byte CANFINISH = 2;     // 可提前
    public final static byte FINISH = 3;        // 已完成
    public final static byte AWARD = 4;         // 已领取

    private long roleId;
    private int missionSlot;    // 任务槽
    private int missionId;      // 任务id
    private int freshStamp;     // 刷新时间戳
    private byte state;         // 任务状态
    private int startStamp;     // 开始时间戳
    private String guestGroup = "";  // 接受任务的门客

    public String getGuestGroup() {
        return guestGroup;
    }

    public void setGuestGroup(String guestGroup) {
        this.guestGroup = guestGroup;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getMissionSlot() {
        return missionSlot;
    }

    public void setMissionSlot(int missionSlot) {
        this.missionSlot = missionSlot;
    }

    public int getMissionId() {
        return missionId;
    }

    public void setMissionId(int missionId) {
        this.missionId = missionId;
    }

    public int getFreshStamp() {
        return freshStamp;
    }

    public void setFreshStamp(int freshStamp) {
        this.freshStamp = freshStamp;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public int getStartStamp() {
        return startStamp;
    }

    public void setStartStamp(int startStamp) {
        this.startStamp = startStamp;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleguestmission", "roleid = " + roleId + " and missionslot = " + missionSlot);
    }

    @Override
    public String getDeleteSql() {
        return "";
    }
}
