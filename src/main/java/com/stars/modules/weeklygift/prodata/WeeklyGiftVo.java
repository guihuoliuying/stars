package com.stars.modules.weeklygift.prodata;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-06-28.
 */
public class WeeklyGiftVo {
    private int weeklyGiftId;
    private int dropId;
    private int order;
    private String vipRange;
    private String levelRange;
    private int chargeNum;
    private int days;
    private int emailTemplateId;

    private int maxVipLv;
    private int minVipLv;
    private int maxLevel;
    private int minLevel;

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeInt(weeklyGiftId);
        buff.writeInt(dropId);
        buff.writeInt(order);
        buff.writeInt(maxVipLv);
        buff.writeInt(minVipLv);
        buff.writeInt(maxLevel);
        buff.writeInt(minLevel);
        buff.writeInt(chargeNum);
        buff.writeInt(days);
        buff.writeInt(emailTemplateId);
    }

    public int getMaxVipLv() {
        return maxVipLv;
    }

    public int getMinVipLv() {
        return minVipLv;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getWeeklyGiftId() {
        return weeklyGiftId;
    }

    public void setWeeklyGiftId(int weeklyGiftId) {
        this.weeklyGiftId = weeklyGiftId;
    }

    public int getDropId() {
        return dropId;
    }

    public void setDropId(int dropId) {
        this.dropId = dropId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getVipRange() {
        return vipRange;
    }

    public void setVipRange(String vipRange) {
        this.vipRange = vipRange;
        String[] tmp = vipRange.split("\\+");
        minVipLv = Integer.parseInt(tmp[0]);
        maxVipLv = Integer.parseInt(tmp[1]);
    }

    public String getLevelRange() {
        return levelRange;
    }

    public void setLevelRange(String levelRange) {
        this.levelRange = levelRange;
        String[] tmp = levelRange.split("\\+");
        minLevel = Integer.parseInt(tmp[0]);
        maxLevel = Integer.parseInt(tmp[1]);
    }

    public int getChargeNum() {
        return chargeNum;
    }

    public void setChargeNum(int chargeNum) {
        this.chargeNum = chargeNum;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getEmailTemplateId() {
        return emailTemplateId;
    }

    public void setEmailTemplateId(int emailTemplateId) {
        this.emailTemplateId = emailTemplateId;
    }

    @Override
    public String toString() {
        return "WeeklyGiftVo{" +
                "weeklyGiftId=" + weeklyGiftId +
                ", dropId=" + dropId +
                ", order=" + order +
                ", vipRange='" + vipRange + '\'' +
                ", levelRange='" + levelRange + '\'' +
                ", chargeNum=" + chargeNum +
                ", days=" + days +
                ", emailTemplateId=" + emailTemplateId +
                '}';
    }
}
