package com.stars.modules.buddy.summary;

import com.stars.services.summary.SummaryComponent;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/10/10.
 */
public interface BuddySummaryComponent extends SummaryComponent {
    public Map<Integer, BuddySummaryVo> getBuddySummaryVoMap();

    public BuddySummaryVo getBuddySummaryVo(int buddyId);

    public BuddySummaryVo getFightBuddySummaryVo();
}
