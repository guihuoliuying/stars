package com.stars.modules.onlinereward.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.onlinereward.OnlineRewardModule;
import com.stars.modules.onlinereward.OnlineRewardPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2017/1/13.
 */
public class ServerOnlineRewardCountDown  extends PlayerPacket {
	@Override
    public short getType() {
        return OnlineRewardPacketSet.S_ONLINE_REWARD_COUNT_DOWN;
    } 
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {	
		
	}
	
    @Override
    public void execPacket(Player player) {
    	OnlineRewardModule onlineRewardModule = (OnlineRewardModule)this.module(MConst.OnlineReward);
        onlineRewardModule.sendCountDownTime();
    } 
}
