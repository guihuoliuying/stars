package com.stars.modules.weeklyCharge.prodata;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

/**
 * Created by chenxie on 2017/5/5.
 */
public class WeeklyChargeVo implements Comparable<WeeklyChargeVo> {

    private int weeklyTotalId;
    private int order;
    private int reward;
    private int totalCharge;
    private int operateActId;
    private String levelRange;
    private int minLevel;
    private int maxLevel;
    private String vipLevelRange;
    private int minVipLevel;
    private int maxVipLevel;

    public int getWeeklyTotalId() {
        return weeklyTotalId;
    }

    public void setWeeklyTotalId(int weeklyTotalId){
        this.weeklyTotalId = weeklyTotalId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
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

    @Override
    public int compareTo(WeeklyChargeVo info) {
        if(this.getOrder() != info.getOrder()){
            return this.getOrder() - info.getOrder();
        }
        return 1;
    }

    public void writeToBuff(NewByteBuffer buff){
        buff.writeInt(weeklyTotalId);
        buff.writeInt(order);
        buff.writeInt(reward);
        buff.writeInt(totalCharge);
    }

    public boolean matchLevel(int roleLevel) {
        return (this.minLevel <= roleLevel) && (this.maxLevel >= roleLevel);
    }

    public boolean matchVipLevel(int vipLevel) {
        return (this.minVipLevel <= vipLevel) && (this.maxVipLevel >= vipLevel);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WeeklyChargeVo that = (WeeklyChargeVo) o;

        return weeklyTotalId == that.weeklyTotalId;

    }

    @Override
    public int hashCode() {
        return weeklyTotalId;
    }
}
