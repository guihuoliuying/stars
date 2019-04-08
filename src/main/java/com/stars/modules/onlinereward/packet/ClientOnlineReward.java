package com.stars.modules.onlinereward.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.onlinereward.OnlineRewardManager;
import com.stars.modules.onlinereward.OnlineRewardPacketSet;
import com.stars.modules.onlinereward.prodata.OnlineRewardVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;
import java.util.Set;

/**
 * Created by gaopeidian on 2016/11/21.
 */
public class ClientOnlineReward extends PlayerPacket {
	public static final byte Flag_Reward_Info = 0;
	public static final byte Flag_Get_Reward =1;
	
	private byte flag;
	
	private int onlineTime = 0;
    private Map<Integer, Byte> onlineRewardMap = null;
    
    private int getRewardId;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return OnlineRewardPacketSet.C_ONLINE_REWARD;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	buff.writeByte(flag);
    	switch (flag) {
		case Flag_Reward_Info:
			writeRewardInfo(buff);
			break;
		case Flag_Get_Reward:
			writeGetReward(buff);
			break;
		default:
			break;
		}
    }
    
    private void writeRewardInfo(NewByteBuffer buff){
    	short size = (short) (onlineRewardMap == null ? 0 : onlineRewardMap.size());
        buff.writeShort(size);
        if (size != 0) {
            Set<Map.Entry<Integer , Byte>> entrySet = onlineRewardMap.entrySet();
            for (Map.Entry<Integer , Byte> entry : entrySet) {
            	int rewardId = entry.getKey();
            	OnlineRewardVo onlineRewardVo = OnlineRewardManager.getOnlineRewardVo(rewardId);
            	if (onlineRewardVo == null) {
					continue;
				}
            	byte status = entry.getValue();
				
            
            	buff.writeInt(rewardId);
            	buff.writeInt(onlineRewardVo.getMinute());
            	//奖励
//            	Map<Integer, Integer> rewardMap = onlineRewardVo.getRewardMap();
//            	short size2 = (short) (rewardMap == null ? 0 : rewardMap.size());
//            	buff.writeShort(size2);
//            	Set<Map.Entry<Integer , Integer>> entrySet2 = rewardMap.entrySet();
//                for (Map.Entry<Integer , Integer> entry2 : entrySet2) {
//                	int itemId = entry2.getKey();
//                	int count = entry2.getValue();
//                	buff.writeInt(itemId);
//                	buff.writeInt(count);
//                }
            	buff.writeInt(onlineRewardVo.getGroupId());
            	
            	buff.writeString(onlineRewardVo.getDesc());
            	buff.writeByte(status);
            	buff.writeInt(onlineTime);				
			}
        }   
    }
    
    private void writeGetReward(NewByteBuffer buff){
    	buff.writeInt(getRewardId);
    }
    
    public void setFlag(byte value){
    	this.flag = value;
    }
    
    public void setOnlineRewardMap(Map<Integer, Byte> value){
    	this.onlineRewardMap = value;
    }
    
    public void setOnlineTime(int value){
    	onlineTime = value;
    }
    
    public void setGetRewardId(int value){
    	getRewardId = value;
    }
}