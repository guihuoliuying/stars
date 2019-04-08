package com.stars.modules.foreshow.summary;

import com.stars.services.summary.SummaryComponent;

/**
 * Created by chenkeyu on 2017/1/3 18:53
 */
public interface ForeShowSummaryComponent extends SummaryComponent {

    boolean isOpen(String openname);
}
