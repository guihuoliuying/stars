package com.stars.modules.gamecave.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.gamecave.GameCavePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;

/**
 * Created by gaopeidian on 2016/6/21.
 */
public class ClientRequestGetCard extends PlayerPacket {
	private List<Integer> randomCardIds;
	private int canChooseCount;
	private int score;
	
    public ClientRequestGetCard() {
    	
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return GameCavePacketSet.C_REQUSET_CET_CARD;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	short size = (short) (randomCardIds == null ? 0 : randomCardIds.size());
        buff.writeShort(size);
        if (size != 0) {
            for (Integer cardId : randomCardIds) {
            	buff.writeInt(cardId);
            }
        }
        
        buff.writeInt(canChooseCount);
        buff.writeInt(score);
    }
    
    public void setRandomCardIds(List<Integer> value){
    	randomCardIds = value;
    }
    
    public void setCanChooseCount(int value){
    	canChooseCount = value;
    }
    
    public void setScore(int value){
    	score = value;
    }
}
