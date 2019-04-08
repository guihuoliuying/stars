package com.stars.modules.family.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.family.FamilyPacketSet;

/**
 * Created by zhaowenshuo on 2016/8/30.
 */
public class ServerFamilyAuth extends PlayerPacket {

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyPacketSet.S_AUTH;
    }
}
