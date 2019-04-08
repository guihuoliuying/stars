package com.stars.modules.gamecave.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.gamecave.GameCavePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;

/**
 * Created by gaopeidian on 2016/6/21.
 */
public class ClientChooseGameList extends PlayerPacket {
    private List<Integer> gameIds;

    public ClientChooseGameList() {
    	
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return GameCavePacketSet.C_CHOOSE_GAME_LIST;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        short size = (short) (gameIds == null ? 0 : gameIds.size());
        buff.writeShort(size);
        if (size != 0) {
            for (Integer gameId : gameIds) {
            	buff.writeInt(gameId);
            }
        }
    }
 
    public void setGameIds(List<Integer> value){
    	this.gameIds = value;
    }
}
