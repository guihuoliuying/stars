package com.stars.modules.newdailycharge.prodata;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-07-10.
 */
public class NewDailyChargeInfo implements Comparable<NewDailyChargeInfo> {
    private int newDailyTotalId;
    private int order;
    private String levelRange;
    private String vipLevelRange;
    private int reward;
    private int totalCharge;
    private int days;
    private int operateactid;

    private int minLv;
    private int maxLv;
    private int minVip;
    private int maxVip;

    public int getNewDailyTotalId() {
        return newDailyTotalId;
    }

    public void setNewDailyTotalId(int newDailyTotalId) {
        this.newDailyTotalId = newDailyTotalId;
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
        String[] tmp = levelRange.split("\\+");
        this.minLv = Integer.parseInt(tmp[0]);
        this.maxLv = Integer.parseInt(tmp[1]);
    }

    public String getVipLevelRange() {
        return vipLevelRange;
    }

    public void setVipLevelRange(String vipLevelRange) {
        this.vipLevelRange = vipLevelRange;
        String[] tmp = vipLevelRange.split("\\+");
        this.minVip = Integer.parseInt(tmp[0]);
        this.maxVip = Integer.parseInt(tmp[1]);
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getOperateactid() {
        return operateactid;
    }

    public void setOperateactid(int operateactid) {
        this.operateactid = operateactid;
    }

    public int getMinLv() {
        return minLv;
    }

    public int getMaxLv() {
        return maxLv;
    }

    public int getMinVip() {
        return minVip;
    }

    public int getMaxVip() {
        return maxVip;
    }

    public int getTotalCharge() {
        return totalCharge;
    }

    public void setTotalCharge(int totalCharge) {
        this.totalCharge = totalCharge;
    }

    public boolean matchLevel(int roleLevel) {
        return (this.minLv == 0 || this.minLv <= roleLevel) && (this.maxLv == 0 || this.maxLv >= roleLevel);
    }

    public boolean matchVipLevel(int vipLevel) {
        return (this.minVip == 0 || this.minVip <= vipLevel) && (this.maxVip == 0 || this.maxVip >= vipLevel);
    }

    public boolean matchDays(int day) {
        return this.days == 0 || this.days == day;
    }

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeInt(newDailyTotalId);
        buff.writeInt(order);
        buff.writeInt(reward);
        buff.writeInt(totalCharge);
    }

    @Override
    public String toString() {
        return "NewDailyChargeInfo{" +
                "newDailyTotalId=" + newDailyTotalId +
                ", order=" + order +
                ", levelRange='" + levelRange + '\'' +
                ", vipLevelRange='" + vipLevelRange + '\'' +
                ", reward=" + reward +
                ", totalCharge=" + totalCharge +
                ", days=" + days +
                ", operateactid=" + operateactid +
                ", minLv=" + minLv +
                ", maxLv=" + maxLv +
                ", minVip=" + minVip +
                ", maxVip=" + maxVip +
                '}';
    }

    @Override
    public int compareTo(NewDailyChargeInfo info) {
        if (this.getOrder() != info.getOrder()) {
            return this.getOrder() - info.getOrder();
        }
        return 1;
    }
}
