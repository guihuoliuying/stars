package com.stars.modules.callboss.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.callboss.CallBossPacketSet;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * Created by liuyuheng on 2016/9/8.
 */
public class ServerCallBossRankList extends PlayerPacket {
    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", CallBossPacketSet.S_CALLBOSS_RANKLIST));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        ServiceHelper.callBossService().sendDamageRankList(getRoleId());
    }

    @Override
    public short getType() {
        return CallBossPacketSet.S_CALLBOSS_RANKLIST;
    }
}
