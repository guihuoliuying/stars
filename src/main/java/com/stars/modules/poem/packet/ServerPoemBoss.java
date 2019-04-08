package com.stars.modules.poem.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.poem.PoemModule;
import com.stars.modules.poem.PoemPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2017/1/9.
 */
public class ServerPoemBoss  extends PlayerPacket {
	private int bossDungeonId;
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		bossDungeonId = buff.readInt();
	}
	
    @Override
    public void execPacket(Player player) {
    	PoemModule poemModule = (PoemModule)module(MConst.Poem);
    	poemModule.sendDungeonInfoVo(bossDungeonId);
    }

    @Override
    public short getType() {
        return PoemPacketSet.S_POEM_BOSS;
    }  
}
