package com.stars.services.marry;

import com.stars.services.marry.userdata.MarryRole;

import java.util.Comparator;

/**
 * Created by zhouyaohui on 2016/12/15.
 */
public class ClaimComparator implements Comparator<MarryRole> {
    @Override
    public int compare(MarryRole o1, MarryRole o2) {
        return o2.getClaimStamp() - o1.getClaimStamp();
    }

}
