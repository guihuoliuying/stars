package com.stars.modules.poemdungeon.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.poemdungeon.PoemDungeonModule;
import com.stars.modules.poemdungeon.PoemDungeonPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2017/1/9.
 */
public class ServerPoemDungeon  extends PlayerPacket {
	private int dungeonId;
	
    @Override
    public void execPacket(Player player) {
    	PoemDungeonModule pModule = (PoemDungeonModule)module(MConst.PoemDungeon);
    	pModule.sendTeamInfo(this.dungeonId);
    }

    @Override
    public short getType() {
        return PoemDungeonPacketSet.Server_PoemDungeon;
    } 
    
    @Override
    public void readFromBuffer(NewByteBuffer buff) {
    	dungeonId = buff.readInt();
    }
}
