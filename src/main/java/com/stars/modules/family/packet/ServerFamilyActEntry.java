package com.stars.modules.family.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.family.FamilyPacketSet;
import com.stars.network.server.packet.PacketManager;
import com.stars.util.LogUtil;

/**
 * Created by zhaowenshuo on 2016/10/10.
 */
public class ServerFamilyActEntry extends PlayerPacket {

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", FamilyPacketSet.S_ACT_ENTRY));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        FamilyModule module = (FamilyModule) module(MConst.Family);
        module.sendActEntryList();
    }

    @Override
    public short getType() {
        return FamilyPacketSet.S_ACT_ENTRY;
    }
}
