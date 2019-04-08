package com.stars.modules.vip.event;

import com.stars.core.event.Event;

/**
 * Created by liuyuheng on 2016/12/6.
 */
public class VipLevelupEvent extends Event {
    private int preVipLevel;
    private int newVipLevel;
    private int totalCharge;

    public VipLevelupEvent(int preVipLevel, int newVipLevel) {
        this.preVipLevel = preVipLevel;
        this.newVipLevel = newVipLevel;
    }

    public int getPreVipLevel() {
        return preVipLevel;
    }

    public int getNewVipLevel() {
        return newVipLevel;
    }

    public int getTotalCharge() {
        return totalCharge;
    }

    public void setTotalCharge(int totalCharge) {
        this.totalCharge = totalCharge;
    }
}
