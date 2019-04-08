package com.stars.modules.gamecave.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.gamecave.GameCavePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2016/6/21.
 */
public class ClientTinyGameData extends PlayerPacket {
    private int gameId;
    private int finishRound;
    private int totalRound;
    private byte isGetReward;
    private int star;
    private String dataStr;

    public ClientTinyGameData() {
    	
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return GameCavePacketSet.C_TINY_GAME_DATA;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	buff.writeInt(gameId);
    	buff.writeInt(finishRound);
    	buff.writeInt(totalRound);
        buff.writeByte(isGetReward);        
        buff.writeInt(star);
        buff.writeString(dataStr);
    }
 
    public void setGameId(int value){
    	this.gameId = value;
    }
    
    public void setFinishRound(int value){
    	this.finishRound = value;
    }
    
    public void setTotalRound(int value){
    	this.totalRound = value;
    }
    
    public void setIsGetReward(byte value){
    	this.isGetReward = value;
    }
    
    public void setStar(int value){
    	this.star = value;
    }
    
    public void setDataStr(String value){
    	this.dataStr = value;
    }
}
