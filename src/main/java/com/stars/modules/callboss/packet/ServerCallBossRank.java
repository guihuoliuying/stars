package com.stars.modules.callboss.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.callboss.CallBossPacketSet;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * Created by liuyuheng on 2016/9/8.
 */
public class ServerCallBossRank extends PlayerPacket {
    private int rankUniqueId;

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", CallBossPacketSet.S_CALLBOSS_RANKINFO));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        ServiceHelper.callBossService().sendDamageRank(getRoleId(), rankUniqueId);
    }

    @Override
    public short getType() {
        return CallBossPacketSet.S_CALLBOSS_RANKINFO;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.rankUniqueId = buff.readInt();
    }
}
