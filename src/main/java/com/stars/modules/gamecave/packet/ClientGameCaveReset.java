package com.stars.modules.gamecave.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.gamecave.GameCavePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2017/1/23.
 */
public class ClientGameCaveReset extends PlayerPacket {
    public ClientGameCaveReset() {
    	
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return GameCavePacketSet.C_GAME_CAVE_RESET;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	
    }
}
