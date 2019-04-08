package com.stars.modules.fashion.summary;

import com.stars.modules.fashion.userdata.RoleFashion;
import com.stars.services.summary.SummaryComponent;

/**
 * Created by gaopeidian on 2016/12/16.
 */
public interface FashionSummaryComponent extends SummaryComponent {
    int getDressFashionId();
    RoleFashion getRoleDressFashion();
}
