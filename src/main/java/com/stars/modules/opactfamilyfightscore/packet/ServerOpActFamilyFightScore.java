package com.stars.modules.opactfamilyfightscore.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.opactfamilyfightscore.OpActFamilyFightScorePacketSet;
import com.stars.services.ServiceHelper;

/**
 * Created by chenkeyu on 2017-03-21 20:58
 */
public class ServerOpActFamilyFightScore extends PlayerPacket {
    @Override
    public void execPacket(Player player) {
        ServiceHelper.opActFamilyFightScore().view(player.id());
    }

    @Override
    public short getType() {
        return OpActFamilyFightScorePacketSet.S_OpActFamilyFightScore;
    }
}
