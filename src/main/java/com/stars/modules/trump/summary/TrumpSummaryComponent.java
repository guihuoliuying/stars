package com.stars.modules.trump.summary;

import com.stars.services.summary.SummaryComponent;

import java.util.Map;

/**
 * Created by zhouyaohui on 2016/10/14.
 */
public interface TrumpSummaryComponent extends SummaryComponent {
    Map<Integer, String> getSkillDamageMap();
    Map<Integer, Byte> getPutOnMap();
}
