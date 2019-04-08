package com.stars.modules.family.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.family.FamilyPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.family.main.memdata.RecommendationFamily;
import com.stars.services.family.role.userdata.FamilyRoleApplicationPo;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/24.
 */
public class ClientFamilyRecommendation extends PlayerPacket {

    private List<RecommendationFamily> recommendationList;
    private Map<Long, FamilyRoleApplicationPo> applicationPoMap;

    public ClientFamilyRecommendation() {
    }

    public ClientFamilyRecommendation(List<RecommendationFamily> recommendationList, Map<Long, FamilyRoleApplicationPo> applicationPoMap) {
        this.recommendationList = recommendationList;
        this.applicationPoMap = applicationPoMap;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyPacketSet.C_RECOMM;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte((byte) recommendationList.size());
        for (RecommendationFamily recommendation : recommendationList) {
            recommendation.writeToBuffer(buff);
            buff.writeByte((byte) (applicationPoMap.containsKey(recommendation.getFamilyId()) ? 1 : 0));
        }
    }
}
