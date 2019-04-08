package com.stars.modules.opactfightscore;

import com.stars.modules.opactfightscore.packet.ClientOpActFightScore;
import com.stars.modules.opactfightscore.packet.ServerOpActFightScore;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2017-03-21 10:42
 */
public class OpActFightScorePacketSet extends PacketSet {
    public static short C_OpActFightScore = 0x6219;
    public static short S_OpActFightScore = 0x621A;

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ClientOpActFightScore.class);
        al.add(ServerOpActFightScore.class);
        return al;
    }
}
