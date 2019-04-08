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
public class ServerRequestPlayGame extends PlayerPacket {
	private int gameId;
    @Override
    public short getType() {
        return GameCavePacketSet.S_REQUEST_PLAY_GAME;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
       this.gameId = buff.readInt();
    }
    
    @Override
    public void execPacket(Player player) {
    	GameCaveModule gameCaveModule = (GameCaveModule) module(MConst.GameCave);
        gameCaveModule.requestPlayGame(gameId);
    }
}
