package com.stars.modules.onlinereward.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.onlinereward.OnlineRewardPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;


/**
 * Created by gaopeidian on 2016/11/21.
 */
public class ClientOnlineRewardCountDown extends PlayerPacket {
	private int time = 0;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	buff.writeInt(time);
    }
    
    @Override
    public short getType() {
        return OnlineRewardPacketSet.C_ONLINE_REWARD_COUNT_DOWN;
    }

    public void setTime(int value){
    	time = value;
    }
}