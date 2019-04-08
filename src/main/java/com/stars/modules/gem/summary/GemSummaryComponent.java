package com.stars.modules.gem.summary;

import com.stars.modules.gem.userdata.RoleEquipmentGem;
import com.stars.services.summary.SummaryComponent;

/**
 * Created by zhaowenshuo on 2016/9/20.
 */
public interface GemSummaryComponent extends SummaryComponent {
    public RoleEquipmentGem getRoleGemData();
}
