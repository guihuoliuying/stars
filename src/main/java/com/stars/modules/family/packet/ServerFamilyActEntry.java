package com.stars.modules.family.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.family.FamilyPacketSet;

/**
 * Created by zhaowenshuo on 2016/10/10.
 */
public class ServerFamilyActEntry extends PlayerPacket {

    @Override
    public void execPacket(Player player) {
        FamilyModule module = (FamilyModule) module(MConst.Family);
        module.sendActEntryList();
    }

    @Override
    public short getType() {
        return FamilyPacketSet.S_ACT_ENTRY;
    }
}
