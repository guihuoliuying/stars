package com.stars.modules.achievement.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

/**
 * Created by zhouyaohui on 2016/10/18.
 */
public class AchievementRow extends DbRow {
    /** 常量 */
    public final static byte UNFINISH = 0;  // 未达成
    public final static byte FINISH = 1;    // 达成
    public final static byte ONFINISH = 2;  // 已完成

    private long roleId;
    private int achievementId;
    private byte state;
    private String processing = "";

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getAchievementId() {
        return achievementId;
    }

    public void setAchievementId(int achievementId) {
        this.achievementId = achievementId;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public String getProcessing() {
        return processing;
    }

    public void setProcessing(String processing) {
        this.processing = processing;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleachievement", "`roleid` = " + roleId + " and `achievementid` = " + achievementId);
    }

    @Override
    public String getDeleteSql() {
        return "";
    }
}
