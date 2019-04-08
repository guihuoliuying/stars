package com.stars.services.guest;

import com.stars.modules.guest.userdata.RoleGuestExchange;

import java.util.Comparator;

/**
 * Created by zhouyaohui on 2017/1/16.
 */
public class StampComparator implements Comparator<RoleGuestExchange> {

    private boolean descent;

    public StampComparator(boolean descent) {
        this.descent = descent;
    }

    @Override
    public int compare(RoleGuestExchange o1, RoleGuestExchange o2) {
        if (descent) {
            return o1.getStamp() - o2.getStamp();
        } else {
            return o2.getStamp() - o1.getStamp();
        }
    }
}
