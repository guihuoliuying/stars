package com.stars.modules.giftcome520.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.modules.giftcome520.GiftComeManager;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by huwenjun on 2017/4/17.
 */
public class RoleGiftCome520Po extends DbRow {
    private long roleId;//角色id
    private String rewardtime;// 领取奖励日期，以活动开始计时，存储第几天，以+分割
    private Date lastRewardDateTime;//最后一次领奖时间

    public RoleGiftCome520Po(long roleId, String rewardtime) {
        this.roleId = roleId;
        this.rewardtime = rewardtime;
    }

    public RoleGiftCome520Po() {
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public Date getLastRewardDateTime() {
        if (lastRewardDateTime == null) {
            caculateLastTime();
        }
        return lastRewardDateTime;
    }

    public void caculateLastTime() {
        if (rewardtime.isEmpty()) {
            return;
        }
        String[] rewards = rewardtime.split("\\+");
        Date benginDateTime = GiftComeManager.benginDateTime;
        Calendar beginCalendar = Calendar.getInstance();
        beginCalendar.setTime(benginDateTime);
        beginCalendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(rewards[rewards.length - 1]));
        lastRewardDateTime = beginCalendar.getTime();
    }

    public String getRewardtime() {
        return rewardtime;
    }

    public void setRewardtime(String rewardtime) {
        this.rewardtime = rewardtime;
        caculateLastTime();
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolegiftcome520", "roleid=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return null;
    }
}
