package com.stars.modules.demologin.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.demologin.LoginPacketSet;

/**
 * Created by chenkeyu on 2016/11/18.
 */
public class ClientLogout extends PlayerPacket {
    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return LoginPacketSet.C_LOGOUT;
    }
}
