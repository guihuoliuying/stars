package com.stars.modules.gamecave.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.gamecave.GameCavePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2016/6/21.
 */
public class ClientChooseGame extends PlayerPacket {
    private int gameId;
    private byte result;

    public ClientChooseGame() {
    	
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return GameCavePacketSet.C_CHOOSE_GAME;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(gameId);
        buff.writeByte(result);
    }
 
    public void setGameId(int value){
    	this.gameId = value;
    }
    
    public void setResult(byte value){
    	this.result = value;
    }
}
