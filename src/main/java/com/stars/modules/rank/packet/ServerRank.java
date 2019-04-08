package com.stars.modules.rank.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.rank.RankPacketSet;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.FamilyWarUtil;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.ServiceHelper;
import com.stars.services.rank.RankConstant;

/**
 * Created by liuyuheng on 2016/8/24.
 */
public class ServerRank extends PlayerPacket {
    private int rankId;

    @Override
    public void execPacket(Player player) {
        if (rankId == RankConstant.RANKID_FAMILYTREASURE || rankId == RankConstant.RANKID_FAMILYFIGHTSCORE) {
            FamilyModule family = (FamilyModule) moduleMap().get(MConst.Family);
            long familyId = family.getAuth().getFamilyId();
            ServiceHelper.rankService().sendRankList(rankId, getRoleId(), familyId);
        } else if (rankId == RankConstant.RANKID_CROSS_SERVER_FAMILY_RANK) {
            FamilyModule familyModule = module(MConst.Family);
            MainRpcHelper.familywarRankService().view(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), familyModule.getAuth().getFamilyId(), getRoleId());
        } else {
            ServiceHelper.rankService().sendRankList(rankId, getRoleId());
        }
    }

    @Override
    public short getType() {
        return RankPacketSet.S_RANK;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.rankId = buff.readInt();
    }
}
