package com.stars.modules.luckydraw.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.modules.luckydraw.LuckyDrawManagerFacade;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class RoleLuckyDrawTimePo extends DbRow {
    private long roleId;
    private int dailyTime;
    private int freeTime;
    private int totalDrawTime;
    private long currentEndTime;//本场结束时间
    private int type;//对应活动类型

    public RoleLuckyDrawTimePo(long roleId, long currentEndTime, int actType) {
        this.roleId = roleId;
        this.currentEndTime = currentEndTime;
        this.type = actType;
        reset();
    }

    public RoleLuckyDrawTimePo() {
    }

    public void reset() {
        dailyTime = 0;
        freeTime = LuckyDrawManagerFacade.getLuckyDrawFreeTimes(type);
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleluckydrawtime", "roleid=" + roleId + " and type=" + type);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("roleluckydrawtime", "roleid=" + roleId + " and type=" + type);
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getDailyTime() {
        return dailyTime;
    }

    public void setDailyTime(int dailyTime) {
        this.dailyTime = dailyTime;
    }

    public int getFreeTime() {
        return freeTime;
    }

    public void setFreeTime(int freeTime) {
        this.freeTime = freeTime;
    }

    public int getTotalDrawTime() {
        return totalDrawTime;
    }

    public void setTotalDrawTime(int totalDrawTime) {
        this.totalDrawTime = totalDrawTime;
    }

    public void addTotalDrawTime(int drawTime) {
        this.totalDrawTime += drawTime;
        this.dailyTime += drawTime;
    }

    public long getCurrentEndTime() {
        return currentEndTime;
    }

    public void setCurrentEndTime(long currentEndTime) {
        this.currentEndTime = currentEndTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
