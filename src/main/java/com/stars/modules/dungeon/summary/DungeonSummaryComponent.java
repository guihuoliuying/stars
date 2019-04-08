package com.stars.modules.dungeon.summary;

import com.stars.services.summary.SummaryComponent;

import java.util.Map;

/**
 * Created by gaopeidian on 2017/4/12.
 */
public interface DungeonSummaryComponent extends SummaryComponent {
    Map<Integer, Byte> getDungeonStatusMap();
}
