package com.stars.modules.mind.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.mind.MindModule;
import com.stars.modules.mind.MindPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 客户端请求升级心法(升级也包括激活，即0级到1级)
 * Created by gaopeidian on 2016/9/24.
 */
public class ServerUpgradeMind  extends PlayerPacket {
	private int mindId;
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		mindId = buff.readInt();
	}
	
    @Override
    public void execPacket(Player player) {
    	MindModule mindModule = (MindModule)this.module(MConst.Mind);
    	mindModule.upgradeMind(mindId);
    }

    @Override
    public short getType() {
        return MindPacketSet.S_UPGRADE_MIND;
    }  
}
