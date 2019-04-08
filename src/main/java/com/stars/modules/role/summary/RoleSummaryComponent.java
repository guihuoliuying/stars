package com.stars.modules.role.summary;

import com.stars.core.attr.Attribute;
import com.stars.services.summary.SummaryComponent;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/20.
 */
public interface RoleSummaryComponent extends SummaryComponent {

    String getRoleName();

    int getRoleLevel();

    int getRoleJob();

    int getFightScore();

    int getTitleId();
    
    int getCampType();
    
    int getVigour();

    Attribute getTotalAttr();

    int getChannel();

    void setChannel(int channel);

    Map<String, Integer> getFightScoreMap();

}
