package com.stars.modules.masternotice.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.masternotice.MasterNoticeModule;
import com.stars.modules.masternotice.MasterNoticePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2016/11/21.
 */
public class ServerMasterNotice  extends PlayerPacket {
	private byte opType;
	private int noticeId;

	@Override
    public short getType() {
        return MasterNoticePacketSet.S_MASTER_NOTICE;
    } 
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {	
		opType = buff.readByte();
		if(opType == 1) {		//接受任务
			noticeId = buff.readInt();
		}else if(opType == 2){	//立刻完成任务
			noticeId = buff.readInt();
		}
	}
	
    @Override
    public void execPacket(Player player) {
    	MasterNoticeModule masterNoticeModule = (MasterNoticeModule)this.module(MConst.MasterNotice);
		if(opType == 1) {		//接受任务
			masterNoticeModule.acceptNotice(noticeId);
		}else if(opType == 2){	//立刻完成任务
			masterNoticeModule.finishRightNow(noticeId);
		}
    }
}
