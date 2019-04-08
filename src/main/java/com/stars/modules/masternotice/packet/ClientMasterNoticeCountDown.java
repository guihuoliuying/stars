package com.stars.modules.masternotice.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.masternotice.MasterNoticePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;


/**
 * Created by gaopeidian on 2016/11/22.
 */
public class ClientMasterNoticeCountDown extends PlayerPacket {
	private byte flag;
	private int refreshTime;
    private int leftRefreshCount;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return MasterNoticePacketSet.C_MASTER_NOTICE_COUNT_DOWN;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	buff.writeByte(flag);		
		switch (flag) {
		case MasterNoticePacketSet.Flag_Add_Refresh_Count:
		{
			buff.writeInt(refreshTime);
			buff.writeInt(leftRefreshCount);
			break;
		}
		case MasterNoticePacketSet.Flag_Begin_Count_Down:
		{
			buff.writeInt(refreshTime);
			break;
		}
		default:
			break;
		}
    }
    
    public void setFlag(byte value){
    	this.flag = value;
    }
    
    public void setRefreshTime(int value){
    	this.refreshTime = value;
    }
    
    public void setLeftRefreshCount(int value){
    	this.leftRefreshCount = value;
    }
}