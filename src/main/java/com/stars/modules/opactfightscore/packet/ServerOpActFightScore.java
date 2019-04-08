package com.stars.modules.opactfightscore.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.opactfightscore.OpActFightScorePacketSet;
import com.stars.services.ServiceHelper;

/**
 * Created by chenkeyu on 2017-03-21 21:05
 */
public class ServerOpActFightScore extends PlayerPacket {
    @Override
    public void execPacket(Player player) {
        ServiceHelper.opActFightScore().view(player.id());
    }

    @Override
    public short getType() {
        return OpActFightScorePacketSet.S_OpActFightScore;
    }
}
