package com.stars.modules.demologin.packet;

import com.stars.core.clientpatch.PatchManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.demologin.LoginPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2017/4/25.
 */
public class ClientPatch extends PlayerPacket {

//    private String patch = "";

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return LoginPacketSet.C_PATCH;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(PatchManager.patch);
    }
}
