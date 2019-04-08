package com.stars.services.weeklygift.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-06-28.
 */
public class RoleWeeklyGift extends DbRow {
    private long roleId;
    private long markfinish;
    private int vipLevel;
    private int level;
    private int totalCharge;
    private String giftDays;

    private Map<Integer, Integer> giftDaysMap = new HashMap<>();

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public long getMarkfinish() {
        return markfinish;
    }

    public void setMarkfinish(long markfinish) {
        this.markfinish = markfinish;
    }

    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getTotalCharge() {
        return totalCharge;
    }

    public void setTotalCharge(int totalCharge) {
        this.totalCharge = totalCharge;
    }

    public void addCharge(int charge) {
        this.totalCharge += charge;
    }

    public String getGiftDays() {
        return giftDays;
    }

    public void setGiftDays(String giftDays) {
        this.giftDays = giftDays;
        this.giftDaysMap = StringUtil.toMap(giftDays, Integer.class, Integer.class, '+', '|');
    }

    public Map<Integer, Integer> getGiftDaysMap() {
        return giftDaysMap;
    }

    public boolean containGiftId(int giftId) {
        return giftDaysMap.containsKey(giftId);
    }

    public void addGiftDays(int giftId, int days) {
        this.giftDaysMap.put(giftId, days);
        this.giftDays = StringUtil.makeString(giftDaysMap, '+', '|');
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleweeklygift", " `roleid` = " + this.roleId + " and `markfinish` =" + this.markfinish);
    }

    @Override
    public String getDeleteSql() {
        return "delete from roleweeklygift where roleid = " + this.roleId + " and markfinish = " + this.markfinish;
    }

    @Override
    public String toString() {
        return "RoleWeeklyGift{" +
                "roleId=" + roleId +
                ", vipLevel=" + vipLevel +
                ", level=" + level +
                ", totalCharge=" + totalCharge +
                ", giftDays='" + giftDays + '\'' +
                ", giftDaysMap=" + giftDaysMap +
                '}';
    }
}
