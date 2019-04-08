package com.stars.modules.masternotice.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.masternotice.MasterNoticeModule;
import com.stars.modules.masternotice.MasterNoticePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2016/11/22.
 */
public class ServerMasterNoticeRefresh  extends PlayerPacket {
	@Override
    public short getType() {
        return MasterNoticePacketSet.S_MASTER_NOTICE_REFRESH;
    } 
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {
			
	}
	
    @Override
    public void execPacket(Player player) {
    	MasterNoticeModule masterNoticeModule = (MasterNoticeModule)this.module(MConst.MasterNotice);
    	masterNoticeModule.refresh();
    } 
}
