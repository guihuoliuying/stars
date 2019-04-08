package com.stars.modules.dailyCharge.prodata;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

/**
 * Created by wuyuxing on 2017/3/29.
 */
public class DailyChargeInfo implements Comparable<DailyChargeInfo> {
    private int dailyTotalId;
    private int order;
    private String levelRange;
    private int minLevel;
    private int maxLevel;
    private String vipLevelRange;
    private int minVipLevel;
    private int maxVipLevel;
    private int reward;
    private int totalCharge;
    private int operateActId;
    private int days;

    public int getDailyTotalId() {
        return dailyTotalId;
    }

    public void setDailyTotalId(int dailyTotalId) {
        this.dailyTotalId = dailyTotalId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getLevelRange() {
        return levelRange;
    }

    public void setLevelRange(String levelRange) {
        this.levelRange = levelRange;
        if (StringUtil.isEmptyIncludeZero(levelRange)) return;
        String[] array = levelRange.split("\\+");
        this.minLevel = Integer.parseInt(array[0]);
        this.maxLevel = Integer.parseInt(array[1]);
    }

    public String getVipLevelRange() {
        return vipLevelRange;
    }

    public void setVipLevelRange(String vipLevelRange) {
        this.vipLevelRange = vipLevelRange;
        if (StringUtil.isEmptyIncludeZero(vipLevelRange)) return;
        String[] array = vipLevelRange.split("\\+");
        this.minVipLevel = Integer.parseInt(array[0]);
        this.maxVipLevel = Integer.parseInt(array[1]);
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public int getTotalCharge() {
        return totalCharge;
    }

    public void setTotalCharge(int totalCharge) {
        this.totalCharge = totalCharge;
    }

    public int getOperateActId() {
        return operateActId;
    }

    public void setOperateActId(int operateActId) {
        this.operateActId = operateActId;
    }

    public boolean matchLevel(int roleLevel) {
        return (this.minLevel == 0 || this.minLevel <= roleLevel) && (this.maxLevel == 0 || this.maxLevel >= roleLevel);
    }

    public boolean matchVipLevel(int vipLevel) {
        return (this.minVipLevel == 0 || this.minVipLevel <= vipLevel) && (this.maxVipLevel == 0 || this.maxVipLevel >= vipLevel);
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public boolean matchDays(int day) {
        return this.days == 0 || this.days == day;
    }

    @Override
    public int compareTo(DailyChargeInfo info) {
        if (this.getOrder() != info.getOrder()) {
            return this.getOrder() - info.getOrder();
        }
        return 1;
    }

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeInt(dailyTotalId);
        buff.writeInt(order);
        buff.writeInt(reward);
        buff.writeInt(totalCharge);
    }
}
