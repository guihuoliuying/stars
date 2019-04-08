package com.stars.modules.newserverfightscore;

import com.stars.modules.newserverfightscore.packet.ClientNSFightScore;
import com.stars.modules.newserverfightscore.packet.ServerNSFightScore;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyuheng on 2017/1/4.
 */
public class NewServerFightPacketSet extends PacketSet {
    public static short S_NSFIGHTSCORE = 0x01C8;
    public static short C_NCFIGHTSCORE = 0x01C9;

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerNSFightScore.class);
        list.add(ClientNSFightScore.class);
        return list;
    }
}
