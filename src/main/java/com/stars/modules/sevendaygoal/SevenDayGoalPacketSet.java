package com.stars.modules.sevendaygoal;

import com.stars.modules.sevendaygoal.packet.ClientGoalData;
import com.stars.modules.sevendaygoal.packet.ClientSevenDayGetReward;
import com.stars.modules.sevendaygoal.packet.ServerGoalData;
import com.stars.modules.sevendaygoal.packet.ServerSevenDayGetReward;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaopeidian on 2016/12/5.
 */
public class SevenDayGoalPacketSet extends PacketSet {
    public static short S_GOAL_DATA = 0x01A5;
    public static short C_GOAL_DATA = 0x01A6;
    public static short S_SEVEN_DAY_GET_REWARD = 0x01A7;
    public static short C_SEVEN_DAY_GET_REWARD = 0x01A8;
    
    public SevenDayGoalPacketSet() {

    }

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ServerGoalData.class);
        al.add(ClientGoalData.class);
        al.add(ServerSevenDayGetReward.class);
        al.add(ClientSevenDayGetReward.class);
        return al;
    }
}