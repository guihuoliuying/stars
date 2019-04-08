package com.stars.modules.teampvpgame.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.teampvpgame.TeamPVPGamePacketSet;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

/**
 * Created by liuyuheng on 2016/12/22.
 */
public class ServerTPGEnterFight extends PlayerPacket {
    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", TeamPVPGamePacketSet.S_TPG_ENTERFIGHT));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        ServiceHelper.tpgLocalService().enterFight(getRoleId());
    }

    @Override
    public short getType() {
        return TeamPVPGamePacketSet.S_TPG_ENTERFIGHT;
    }
}
