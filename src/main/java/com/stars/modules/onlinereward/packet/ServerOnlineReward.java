package com.stars.modules.onlinereward.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.onlinereward.OnlineRewardModule;
import com.stars.modules.onlinereward.OnlineRewardPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2016/11/21.
 */
public class ServerOnlineReward  extends PlayerPacket {
	public static final byte Flag_Get_All_Reward_Info = 0;
	public static final byte Flag_Get_One_Reward_Info =1;
	public static final byte Flag_Get_Reward = 2;
	
	private byte flag;
	private int activityId;
	private int rewardId;
	
	@Override
    public short getType() {
        return OnlineRewardPacketSet.S_ONLINE_REWARD;
    } 
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {	
		flag = buff.readByte();
		if (flag == Flag_Get_All_Reward_Info) {
			activityId = buff.readInt();
		}else if (flag == Flag_Get_One_Reward_Info || flag == Flag_Get_Reward) {
			activityId = buff.readInt();
			rewardId = buff.readInt();
		}
	}
	
    @Override
    public void execPacket(Player player) {
    	OnlineRewardModule onlineRewardModule = (OnlineRewardModule)this.module(MConst.OnlineReward);
    	switch (flag) {
		case Flag_Get_All_Reward_Info:
			onlineRewardModule.sendRewards(activityId);
			break;
		case Flag_Get_One_Reward_Info:
			onlineRewardModule.sendReward(activityId , rewardId);
			break;
		case Flag_Get_Reward:
			onlineRewardModule.getReward(activityId, rewardId);
			break;
		default:
			break;
		}
    } 
}
