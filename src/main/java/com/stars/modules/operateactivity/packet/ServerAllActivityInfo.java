package com.stars.modules.operateactivity.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.OperateActivityPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2016/11/21.
 */
public class ServerAllActivityInfo  extends PlayerPacket {
	@Override
    public short getType() {
        return OperateActivityPacketSet.S_ALL_ACTIVITY_INFO;
    } 
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {	
		
	}
	
    @Override
    public void execPacket(Player player) {
    	OperateActivityModule opActivityModule = (OperateActivityModule)this.module(MConst.OperateActivity);
    	opActivityModule.sendAllActivityInfo();
    } 
}
