package com.stars.modules.elitedungeon.summary;

import com.stars.services.summary.SummaryComponent;

/**
 * Created by gaopeidian on 2017/4/19.
 */
public interface EliteDungeonSummaryComponent extends SummaryComponent {
    int getPlayCount();
    int getRewardTimes();
    int getHelpTimes();
}
