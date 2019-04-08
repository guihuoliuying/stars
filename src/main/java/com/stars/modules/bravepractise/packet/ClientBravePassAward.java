package com.stars.modules.bravepractise.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.bravepractise.BravePractisePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;
import java.util.Set;


/**
 * Created by gaopeidian on 2016/11/17.
 */
public class ClientBravePassAward extends PlayerPacket {
    private Map<Integer, Integer> rewardMap;
    private byte type;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return BravePractisePacketSet.C_PASS_AWARD;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(type);
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
    
    public void setRewardMap(Map<Integer, Integer> rewardMap){
    	this.rewardMap = rewardMap;
    }

    public void setType(byte type) {
        this.type = type;
    }
}