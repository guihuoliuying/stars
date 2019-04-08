package com.stars.modules.newequipment.summary;

import com.stars.modules.newequipment.userdata.RoleEquipment;
import com.stars.services.summary.SummaryComponent;

import java.util.List;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/11/8.
 */
public interface NewEquipmentSummaryComponent extends SummaryComponent {

    Map<Byte, RoleEquipment> getEquipmentMap();

    RoleEquipment getEquipInfoByType(Byte type);

    public List<String> getDragonBallList();

}
