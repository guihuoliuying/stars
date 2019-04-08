package com.stars.modules.newserverrank.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.newserverrank.NewServerRankPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.ServiceHelper;

/**
 * Created by gaopeidian on 2016/11/21.
 */
public class ServerNewServerRank  extends PlayerPacket {
	public static final byte Flag_Get_Reward_Info = 0;
	public static final byte Flag_Get_Rank_Info = 1;
	
	private byte flag;
	private int activityId;
	
	@Override
    public short getType() {
        return NewServerRankPacketSet.S_NEW_SERVER_RANK;
    } 
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {	
		flag = buff.readByte();
		activityId = buff.readInt();
	}
	
    @Override
    public void execPacket(Player player) {
    	switch (flag) {
		case Flag_Get_Reward_Info:
			ServiceHelper.newServerRankService().getRewardInfo(activityId, player.id());
			break;
		case Flag_Get_Rank_Info:
			ServiceHelper.newServerRankService().getRankInfo(activityId, player.id());
			break;
		default:
			break;
		}
    } 
}
