package com.stars.modules.activeweapon.usrdata;

import com.google.common.collect.Maps;
import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by huwenjun on 2017/6/15.
 */
public class RoleActiveWeapon extends DbRow {
    private long roleId;
    private String onlineDays;
    private String reward;//(conditionId+1/0)+|

    private Set<String> onlineDaySet = new HashSet<>();
    /**
     * 《conditionId，状态》
     * 状态
     * -1：未激活
     * 1：已激活，可领取
     * 0：已领取
     */
    private Map<Integer, Byte> rewardRecord = Maps.newHashMap();

    public RoleActiveWeapon() {
    }

    public RoleActiveWeapon(long roleId, String onlineDays, String reward) {
        this.roleId = roleId;
        this.onlineDays = onlineDays;
        this.reward = reward;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleactiveweapon", "roleid=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("roleactiveweapon", "roleid=" + roleId);
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getOnlineDays() {
        return onlineDays;
    }

    public void setOnlineDays(String onlineDays) {
        this.onlineDays = onlineDays;
        try {
            onlineDaySet = StringUtil.toHashSet(onlineDays, String.class, '+');
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
        this.rewardRecord = StringUtil.toMap(this.reward, Integer.class, Byte.class, '+', '|');
    }

    public boolean isDefault() {
        return StringUtil.isEmpty(onlineDays) && StringUtil.isEmpty(reward);
    }

    public Set<String> getOnlineDaySet() {
        return onlineDaySet;
    }

    public void addOnlineDay(String dateStr) {
        if (onlineDaySet.size() > 7) {
            return;
        }
        onlineDaySet.add(dateStr);
        onlineDays = StringUtil.makeString(onlineDaySet, '+');
    }

    public Map<Integer, Byte> getRewardRecord() {
        return rewardRecord;
    }

    public void setRewardRecord(Map<Integer, Byte> rewardRecord) {
        this.rewardRecord = rewardRecord;
        this.reward = StringUtil.makeString(this.rewardRecord, '+', '|');
    }

    public void updateRewardRecord(Integer conditionId, Byte isTaked) {
        this.rewardRecord.put(conditionId, isTaked);
        setRewardRecord(this.rewardRecord);
    }
}
