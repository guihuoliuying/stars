package com.stars.modules.sevendaygoal.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.sevendaygoal.SevenDayGoalModule;
import com.stars.modules.sevendaygoal.SevenDayGoalPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2016/12/19.
 */
public class ServerSevenDayGetReward  extends PlayerPacket {
	private int activityId;
	private int days;
	private int goalId;
	
	@Override
    public short getType() {
        return SevenDayGoalPacketSet.S_SEVEN_DAY_GET_REWARD;
    } 
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {	
		activityId = buff.readInt();
		days = buff.readInt();
		goalId = buff.readInt();
	}
	
    @Override
    public void execPacket(Player player) {
    	SevenDayGoalModule sevenDayGoalModule = (SevenDayGoalModule)this.module(MConst.SevenDayGoal);
    	sevenDayGoalModule.getReward(activityId, days, goalId);
    } 
}
