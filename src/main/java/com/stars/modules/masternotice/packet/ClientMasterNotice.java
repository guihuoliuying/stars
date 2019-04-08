package com.stars.modules.masternotice.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.masternotice.MasterNoticePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;
import java.util.Set;


/**
 * Created by gaopeidian on 2016/11/21.
 */
public class ClientMasterNotice extends PlayerPacket {
    private int noticeId;
    private byte noticeStatus;
    private int leftCount;  
    private Map<Integer, Integer> rewardMap = null;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return MasterNoticePacketSet.C_MASTER_NOTICE;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	buff.writeInt(noticeId);
		buff.writeByte(noticeStatus);
		buff.writeInt(leftCount);
		
	  	//奖励
    	short size = (short) (rewardMap == null ? 0 : rewardMap.size());
        buff.writeShort(size);
        if (size != 0) {
            Set<Map.Entry<Integer , Integer>> entrySet = rewardMap.entrySet();
            for (Map.Entry<Integer , Integer> entry : entrySet) {
				int itemId = entry.getKey();
				int itemCount = entry.getValue();
				buff.writeInt(itemId);
				buff.writeInt(itemCount);
			}
        }
    }
    
    public void setNoticeId(int value){
    	this.noticeId = value;
    }
    
    public void setNoticeStatus(byte value){
    	this.noticeStatus = value;
    }
    
    public void setLeftCount(int value){
    	this.leftCount = value;
    }
    
    public void setRewardMap(Map<Integer, Integer> value){
    	this.rewardMap = value;
    }
}