package com.stars.modules.title.summary;

import com.stars.services.summary.SummaryComponent;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/20.
 */
public interface TitleSummaryComponent extends SummaryComponent {

    Map<Byte, Integer> getTopFightScoreTitleMap();

    void setTopFightScoreTitleMap(Map<Byte, Integer> topFightScoreTitleMap);

}
