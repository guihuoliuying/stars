package com.stars.modules.family.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.family.FamilyPacketSet;
import com.stars.services.ServiceHelper;
import com.stars.services.family.main.FamilyMainServiceActor;
import com.stars.services.family.main.memdata.RecommendationFamily;

import java.util.List;

/**
 * Created by zhaowenshuo on 2016/8/24.
 */
public class ServerFamilyRecommendation extends PlayerPacket {

    @Override
    public void execPacket(Player player) {
        List<RecommendationFamily> recommList = FamilyMainServiceActor.recommList;
        if (recommList != null && recommList.size() == 0) {
            FamilyModule familyModule = module(MConst.Family);
            familyModule.setFreeQuota(true);
        }
        ServiceHelper.familyRoleService().sendRecommendationList(getRoleId());
    }

    @Override
    public short getType() {
        return FamilyPacketSet.S_RECOMM;
    }
}
