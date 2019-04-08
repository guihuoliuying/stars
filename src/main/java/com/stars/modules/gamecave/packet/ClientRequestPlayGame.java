package com.stars.modules.gamecave.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.gamecave.GameCavePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2016/6/21.
 */
public class ClientRequestPlayGame extends PlayerPacket {
	private int gameId;
	private int roundId;
	private byte result;
	
    public ClientRequestPlayGame() {
    	
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return GameCavePacketSet.C_REQUEST_PLAY_GAME;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(gameId);
        buff.writeInt(roundId);
        buff.writeByte(result);
    }
    
    public void setGameId(int value){
    	gameId = value;
    }
    
    public void setRoundId(int value){
    	roundId = value;
    }
    
    public void setResult(byte value){
    	result = value;
    }
}
