package com.stars.modules.gamecave.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.gamecave.GameCaveModule;
import com.stars.modules.gamecave.GameCavePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2017/1/16.
 */
public class ServerFinishGame extends PlayerPacket {
	private int gameId;
	private int roundId;
	private String dataStr;
	
    @Override
    public short getType() {
        return GameCavePacketSet.S_FINISH_GAME;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
       this.gameId = buff.readInt();
       this.roundId = buff.readInt();
       this.dataStr = buff.readString();
    }
    
    @Override
    public void execPacket(Player player) {
    	GameCaveModule gameCaveModule = (GameCaveModule) module(MConst.GameCave);
        gameCaveModule.finishGame(gameId, roundId, dataStr);
    }
}
