package com.stars.modules.newfirstrecharge.usrdata;

import com.stars.core.module.Module;
import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.modules.MConst;
import com.stars.modules.vip.VipModule;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/9/6.
 */
public class RoleNewFirstRecharge extends DbRow {
    private long roleId;
    private int activityType;
    private String activityData;//day+status|
    private long lastActivityEndTime;
    private int payCount;//充值数量
    private int today;
    private long lastTakeTime;//上次领取时间戳
    private int vipLevel;//vip等级
    private int group = 0;//第几组奖励
    private Map<Integer, Integer> activityDataMap = new LinkedHashMap<>();

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolenewfirstrecharge", String.format(" roleid =%s and activityType=%s", roleId, activityType));
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolenewfirstrecharge", String.format(" roleid =%s and activityType=%s", roleId, activityType));
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getActivityType() {
        return activityType;
    }

    public void setActivityType(int activityType) {
        this.activityType = activityType;
    }

    public String getActivityData() {
        return activityData;
    }

    public void setActivityData(String activityData) {
        this.activityData = activityData;
        try {
            activityDataMap = StringUtil.toLinkedHashMap(activityData, Integer.class, Integer.class, '+', '|');
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    public long getLastActivityEndTime() {
        return lastActivityEndTime;
    }

    public void setLastActivityEndTime(long lastActivityEndTime) {
        this.lastActivityEndTime = lastActivityEndTime;
    }

    public Map<Integer, Integer> getActivityDataMap() {
        return activityDataMap;
    }

    public void setDayStatus(int day, int status) {
        activityDataMap.put(day, status);
        activityData = StringUtil.makeString(activityDataMap, '+', '|');
    }

    public int getDayStatus(int day) {
        return activityDataMap.get(day);
    }

    public int getPayCount() {
        return payCount;
    }

    public void setPayCount(int payCount) {
        this.payCount = payCount;
    }

    public void reset(Map<String, Module> moduleMap) {
        payCount = 0;
        VipModule vipModule = (VipModule) moduleMap.get(MConst.Vip);
        int vipLevel = vipModule.getVipLevel();
        setVipLevel(vipLevel);
        vipModule.context().update(this);
    }

    public int getToday() {
        return today;
    }

    public void setToday(int today) {
        if (today > 7) {
            setToday(1);
        }
        this.today = today;
    }

    public long getLastTakeTime() {
        return lastTakeTime;
    }

    public void setLastTakeTime(long lastTakeTime) {
        this.lastTakeTime = lastTakeTime;
    }

    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }

    public void addPayCount(int money) {
        payCount += money;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }
}
