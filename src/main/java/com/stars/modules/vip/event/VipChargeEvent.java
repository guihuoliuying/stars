package com.stars.modules.vip.event;

import com.stars.core.event.Event;

/**
 * Created by liuyuheng on 2016/12/19.
 */
public class VipChargeEvent extends Event {
    private int chargeId;
    private int money;
    private String orderNo;
    private boolean isFirst;
    private byte payPoint;
    private int actionType;
    private int lastVipLevel;

    public VipChargeEvent(String orderNo, int money, int chargeId, boolean isFirst, byte payPoint, int actionType,int lastVipLevel) {
        this.chargeId = chargeId;
        this.orderNo = orderNo;
        this.money = money;
        this.isFirst = isFirst;
        this.payPoint = payPoint;
        this.actionType = actionType;
        this.lastVipLevel = lastVipLevel;
    }

    public int getChargeId() {
        return chargeId;
    }

    public void setChargeId(int chargeId) {
        this.chargeId = chargeId;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean isFirst) {
        this.isFirst = isFirst;
    }

    public byte getPayPoint() {
        return this.payPoint;
    }

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }

    public int getLastVipLevel() {
        return lastVipLevel;
    }

    public void setLastVipLevel(int lastVipLevel) {
        this.lastVipLevel = lastVipLevel;
    }
}
