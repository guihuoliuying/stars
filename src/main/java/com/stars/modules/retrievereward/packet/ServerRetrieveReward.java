package com.stars.modules.retrievereward.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.retrievereward.RetrieveRewardModule;
import com.stars.modules.retrievereward.RetrieveRewardPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2016/11/21.
 */
public class ServerRetrieveReward  extends PlayerPacket {
	public static final byte Flag_Get_All_Reward_Info = 0;
	public static final byte Flag_Get_Reward = 1;
	
	private byte flag;
	private int activityId;
	private int rewardId;
	
	@Override
    public short getType() {
        return RetrieveRewardPacketSet.S_RETRIEVE_REWARD;
    } 
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {	
		flag = buff.readByte();
		if (flag == Flag_Get_All_Reward_Info) {
			activityId = buff.readInt();
		}else if (flag == Flag_Get_Reward) {
			activityId = buff.readInt();
			rewardId = buff.readInt();
		}
	}
	
    @Override
    public void execPacket(Player player) {
    	RetrieveRewardModule retrieveRewardModule = (RetrieveRewardModule)this.module(MConst.RetrieveReward);
    	switch (flag) {
		case Flag_Get_All_Reward_Info:
			retrieveRewardModule.sendRewards(activityId);
			break;
		case Flag_Get_Reward:
			retrieveRewardModule.getReward(activityId, rewardId);
			break;
		default:
			break;
		}
    } 
}
