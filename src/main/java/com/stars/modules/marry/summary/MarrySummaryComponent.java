package com.stars.modules.marry.summary;

import com.stars.services.summary.SummaryComponent;

/**
 * Created by zhanghaizhen on 2017/7/3.
 */
public interface MarrySummaryComponent extends SummaryComponent {
    long getCoupleRoleId();
    byte getMarryState();
    void setCoupleRoleId(long coupleRoleId);
    void setMarryState(byte marryState);

}
