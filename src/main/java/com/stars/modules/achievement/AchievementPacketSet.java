package com.stars.modules.achievement;

import com.stars.modules.achievement.packet.ClientAchievement;
import com.stars.modules.achievement.packet.ServerAchievement;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouyaohui on 2016/10/17.
 */
public class AchievementPacketSet extends PacketSet {
    public static final short S_ACHIEVEMENT = 0x0156;
    public static final short C_ACHIEVEMENT = 0x0157;

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerAchievement.class);
        list.add(ClientAchievement.class);
        return list;
    }
}
