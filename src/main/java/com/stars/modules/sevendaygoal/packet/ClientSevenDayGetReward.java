package com.stars.modules.sevendaygoal.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.sevendaygoal.SevenDayGoalPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;


/**
 * Created by gaopeidian on 2016/11/21.
 */
public class ClientSevenDayGetReward extends PlayerPacket {
	private int days;
	private int goalId;
	private byte isGot;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return SevenDayGoalPacketSet.C_SEVEN_DAY_GET_REWARD;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	buff.writeInt(days);
    	buff.writeInt(goalId);
    	buff.writeByte(isGot);
    }
    
    public void setDays(int value){
    	days = value;
    }
    
    public void setGoalId(int value){
    	goalId = value;
    }
    
    public void setIsGot(byte value){
    	isGot = value;
    }
}