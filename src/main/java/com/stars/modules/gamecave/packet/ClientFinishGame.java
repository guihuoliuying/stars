package com.stars.modules.gamecave.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.gamecave.GameCavePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2016/6/21.
 */
public class ClientFinishGame extends PlayerPacket {
	private byte result;
	
    public ClientFinishGame() {
    	
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return GameCavePacketSet.C_FINISH_GAME;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	buff.writeByte(result);
    }
    
    public void setResult(byte value){
    	result = value;
    }
}
