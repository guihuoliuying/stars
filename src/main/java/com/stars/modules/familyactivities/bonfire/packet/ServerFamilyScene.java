package com.stars.modules.familyactivities.bonfire.packet;

import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.familyactivities.bonfire.FamilyBonfireModule;
import com.stars.modules.familyactivities.bonfire.FamilyBonfirePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.util.LogUtil;

/**
 * Created by zhouyaohui on 2016/10/9.
 */
public class ServerFamilyScene extends PlayerPacket {

    @Override
    public short getType() {
        return FamilyBonfirePacketSet.S_FAMILY_SCENE;
    }

    @Override
    public void execPacket(Player player) {
        if (SpecialAccountManager.isSpecialAccount(player.id())) {
            LogUtil.info("SpecialAccountPacketType:{}", String.format("0x%04X", FamilyBonfirePacketSet.S_FAMILY_SCENE));
            PacketManager.send(player.id(), new ClientText("common_tips_cantcontrol"));
            return;
        }
        FamilyBonfireModule bm = (FamilyBonfireModule) module(MConst.FamilyActBonfire);
        bm.enterBonefireScene();
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }
}
