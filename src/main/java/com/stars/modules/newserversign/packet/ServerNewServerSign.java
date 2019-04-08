package com.stars.modules.newserversign.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.newserversign.NewServerSignModule;
import com.stars.modules.newserversign.NewServerSignPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2016/11/21.
 */
public class ServerNewServerSign  extends PlayerPacket {
	public static final byte Flag_Get_Reward_Info = 0;
	public static final byte Flag_Get_Reward = 1;
	
	private byte flag;
	
	//flag = 0 , flag = 1
	private int activityId;
	
	//flag = 1
	private int newServerSignId;
	
	@Override
    public short getType() {
        return NewServerSignPacketSet.S_NEW_SERVER_SIGN;
    } 
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		flag = buff.readByte();		
		switch (flag) {
		case Flag_Get_Reward_Info:{
			activityId = buff.readInt();
			break;
		}			
		case Flag_Get_Reward:{
			activityId = buff.readInt();
			newServerSignId = buff.readInt();
			break;
		}
		default:
			break;
		}	
	}
	
    @Override
    public void execPacket(Player player) {
    	NewServerSignModule newServerSignModule = (NewServerSignModule)this.module(MConst.NewServerSign);
    	switch (flag) {
		case Flag_Get_Reward_Info:
			newServerSignModule.getRewardsInfo(activityId);
			break;
		case Flag_Get_Reward:
			newServerSignModule.getReward(activityId, newServerSignId);
			break;
		default:
			break;
		}
    } 
}
