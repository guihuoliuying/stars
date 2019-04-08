package com.stars.modules.onlinereward;

import com.stars.modules.onlinereward.packet.ClientOnlineReward;
import com.stars.modules.onlinereward.packet.ClientOnlineRewardCountDown;
import com.stars.modules.onlinereward.packet.ServerOnlineReward;
import com.stars.modules.onlinereward.packet.ServerOnlineRewardCountDown;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaopeidian on 2016/12/5.
 */
public class OnlineRewardPacketSet extends PacketSet {
    public static short S_ONLINE_REWARD = 0x0198;
    public static short C_ONLINE_REWARD = 0x0199;
    public static short S_ONLINE_REWARD_COUNT_DOWN = 0x019A;
    public static short C_ONLINE_REWARD_COUNT_DOWN = 0x019B;
    
    public OnlineRewardPacketSet() {

    }

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ServerOnlineReward.class);
        al.add(ClientOnlineReward.class);
        al.add(ServerOnlineRewardCountDown.class);
        al.add(ClientOnlineRewardCountDown.class);
        return al;
    }
}