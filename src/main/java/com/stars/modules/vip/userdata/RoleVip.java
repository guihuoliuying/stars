package com.stars.modules.vip.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/12/5.
 */
public class RoleVip extends DbRow {
    private long roleId;
    private int rewardedVipLv;// 已发放vip等级奖励
    private String firstChargeReward;// 首充奖励领取,格式:chargeId=1/0;1=已领取;0=未领取
    private int monthCardRest;// 月卡奖励领取剩余天数
    private long lastMonthCardRewardTime;// 上次领取月卡奖励时间
    private byte monthCardRewardStatus;// 月卡奖励每日领取状态
    private byte dailySendAnnouncement;// 每日登陆全服滚屏
    private int dailyChargeSum;


    /* 内存数据 */
    private int totalCharge;// 累积充值
    private Map<Integer, Byte> firstChargeRewardMap = new HashMap<>();// 首充奖励领取记录

    public RoleVip() {
    }

    public RoleVip(long roleId) {
        this.roleId = roleId;
        this.rewardedVipLv = 0;
        this.firstChargeReward = "";
        this.lastMonthCardRewardTime = 0;
        this.monthCardRewardStatus = 0;
        this.monthCardRest = 0;
        this.totalCharge = 0;
        this.dailySendAnnouncement = 0;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolevip", " roleid=" + this.getRoleId());
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolevip", " roleid=" + this.getRoleId());
    }

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeInt(totalCharge);// 累积充值
        buff.writeInt(monthCardRest);// 月卡奖励领取剩余天数
        buff.writeByte(monthCardRewardStatus);// 月卡奖励每日领取状态
        // 首充奖励获得记录
        byte size = (byte) (firstChargeRewardMap == null ? 0 : firstChargeRewardMap.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        for (Map.Entry<Integer, Byte> entry : firstChargeRewardMap.entrySet()) {
            buff.writeInt(entry.getKey());// chargeId
            buff.writeByte(entry.getValue());// 状态,1=已领取
        }
    }

    /**
     * 增加首充记录
     *
     * @param chargeId
     */
    public boolean addFirstChargeRecord(int chargeId) {
        if (firstChargeRewardMap.containsKey(chargeId)) {
            return false;
        }
        firstChargeRewardMap.put(chargeId, (byte) 1);
        parseFirstChargeRewardStr();
        return true;
    }

    /**
     * 是否首充
     *
     * @param chargeId
     * @return
     */
    public boolean isFirstCharge(int chargeId) {
        if (!firstChargeRewardMap.containsKey(chargeId)) {
            return true;
        }
        return false;
    }

    private void parseFirstChargeRewardStr() {
        StringBuilder builder = new StringBuilder("");
        for (Map.Entry<Integer, Byte> entry : firstChargeRewardMap.entrySet()) {
            builder.append(";")
                    .append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.indexOf(";"));
        }
        this.firstChargeReward = builder.toString();
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getRewardedVipLv() {
        return rewardedVipLv;
    }

    public void setRewardedVipLv(int rewardedVipLv) {
        this.rewardedVipLv = rewardedVipLv;
    }

    public String getFirstChargeReward() {
        return firstChargeReward;
    }

    public void setFirstChargeReward(String firstChargeReward) throws Exception {
        this.firstChargeReward = firstChargeReward;
        if (StringUtil.isEmpty(firstChargeReward)) {
            firstChargeRewardMap = new HashMap<>();
            return;
        }
        firstChargeRewardMap = StringUtil.toMap(firstChargeReward, Integer.class, Byte.class, '=', ';');
    }

    public long getLastMonthCardRewardTime() {
        return lastMonthCardRewardTime;
    }

    public void setLastMonthCardRewardTime(long lastMonthCardRewardTime) {
        this.lastMonthCardRewardTime = lastMonthCardRewardTime;
    }

    public byte getMonthCardRewardStatus() {
        return monthCardRewardStatus;
    }

    public void setMonthCardRewardStatus(byte monthCardRewardStatus) {
        this.monthCardRewardStatus = monthCardRewardStatus;
    }

    public int getMonthCardRest() {
        return monthCardRest;
    }

    public void setMonthCardRest(int monthCardRest) {
        this.monthCardRest = monthCardRest;
    }

    public int getTotalCharge() {
        return totalCharge;
    }

    public void setTotalCharge(int totalCharge) {
        this.totalCharge = totalCharge;
    }

    public byte getDailySendAnnouncement() {
        return dailySendAnnouncement;
    }

    public void setDailySendAnnouncement(byte dailySendAnnouncement) {
        this.dailySendAnnouncement = dailySendAnnouncement;
    }

    public int getDailyChargeSum() {
        return dailyChargeSum;
    }

    public void setDailyChargeSum(int dailyChargeSum) {
        this.dailyChargeSum = dailyChargeSum;
    }

    public void addDailyCharge(int charge) {
        this.dailyChargeSum += charge;
    }
}
