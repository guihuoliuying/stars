package com.stars.modules.gamecave.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.gamecave.GameCavePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;

/**
 * Created by gaopeidian on 2016/6/21.
 */
public class ClientGetAllCard extends PlayerPacket {
    private List<Integer> cardIds;

    public ClientGetAllCard() {
    	
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return GameCavePacketSet.C_GET_ALL_CARD;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        short size = (short) (cardIds == null ? 0 : cardIds.size());
        buff.writeShort(size);
        if (size != 0) {
            for (Integer cardId : cardIds) {
            	buff.writeInt(cardId);
            }
        }
    }
 
    public void setCardIds(List<Integer> value){
    	this.cardIds = value;
    }
}
