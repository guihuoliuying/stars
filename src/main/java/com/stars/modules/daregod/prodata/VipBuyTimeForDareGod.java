package com.stars.modules.daregod.prodata;

/**
 * Created by chenkeyu on 2017-08-23.
 */
public class VipBuyTimeForDareGod {
    private int minVipLv;
    private int maxVipLv;
    private int times;

    public boolean matchVipLv(int vipLevel) {
        return vipLevel <= maxVipLv && vipLevel >= minVipLv;
    }

    public int getTimes() {
        return times;
    }

    public void setMinVipLv(int minVipLv) {
        this.minVipLv = minVipLv;
    }

    public void setMaxVipLv(int maxVipLv) {
        this.maxVipLv = maxVipLv;
    }

    public void setTimes(int times) {
        this.times = times;
    }
}
