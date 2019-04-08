package com.stars.modules.gamecave.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.gamecave.GameCavePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;

/**
 * Created by gaopeidian on 2016/6/21.
 */
public class ClientGameCaveData extends PlayerPacket {
    private int curGameId;
    private int leftCount;
    private byte isGetReward;
    private int score;
    private List<Integer> curCardIds;
    private List<Integer> finishGameIds;

    public ClientGameCaveData() {
    	
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return GameCavePacketSet.C_GAMECAVE_DATA;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	buff.writeInt(curGameId);
    	buff.writeInt(leftCount);
        buff.writeByte(isGetReward);        
        buff.writeInt(score);
        
        short size = (short) (curCardIds == null ? 0 : curCardIds.size());
        buff.writeShort(size);
        if (size != 0) {
            for (Integer cardId : curCardIds) {
            	buff.writeInt(cardId);
            }
        }
        
        short size2 = (short) (finishGameIds == null ? 0 : finishGameIds.size());
        buff.writeShort(size2);
        if (size2 != 0) {
            for (Integer gameId : finishGameIds) {
            	buff.writeInt(gameId);
            }
        }
    }
 
    public void setCurGameId(int value){
    	this.curGameId = value;
    }
    
    public void setLeftCount(int value){
    	this.leftCount = value;
    }
    
    public void setIsGetReward(byte value){
    	this.isGetReward = value;
    }
    
    public void setScore(int value){
    	this.score = value;
    }
    
    public void setCurCardIds(List<Integer> value){
    	this.curCardIds = value;
    }
    
    public void setFinishGameIds(List<Integer> value){
    	this.finishGameIds = value;
    }
}
