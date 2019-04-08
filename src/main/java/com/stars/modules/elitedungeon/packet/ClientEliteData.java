package com.stars.modules.elitedungeon.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.elitedungeon.EliteDungeonPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;
import java.util.Set;

/**
 * Created by gaopeidian on 2017/4/11.
 */
public class ClientEliteData extends PlayerPacket {
	//<eliteId,status>
	private Map<Integer, Byte> eliteDatas;
	private int playCount;
	private int rewardTimes;
	private int helpTimes;
	
    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return EliteDungeonPacketSet.Client_EliteData;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	short size = (short) (eliteDatas == null ? 0 : eliteDatas.size());
        buff.writeShort(size);
        if (size != 0) {
            Set<Map.Entry<Integer , Byte>> entrySet = eliteDatas.entrySet();
            for (Map.Entry<Integer , Byte> entry : entrySet) {
				int eliteId = entry.getKey();
				byte status = entry.getValue();
				buff.writeInt(eliteId);
				buff.writeByte(status);
			}
        }
        
        buff.writeInt(playCount);
        buff.writeInt(rewardTimes);
        buff.writeInt(helpTimes);
    }
    
    public void setEliteDatas(Map<Integer, Byte> eliteDatas){
    	this.eliteDatas = eliteDatas;
    }
    
    public void setPlayCount(int playCount){
    	this.playCount = playCount;
    }
    
    public void setRewardTimes(int rewardTimes){
    	this.rewardTimes = rewardTimes;
    }
    
    public void setHelpTimes(int helpTimes){
    	this.helpTimes = helpTimes;
    }
}
