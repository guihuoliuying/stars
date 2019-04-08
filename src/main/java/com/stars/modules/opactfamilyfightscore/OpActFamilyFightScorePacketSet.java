package com.stars.modules.opactfamilyfightscore;

import com.stars.modules.opactfamilyfightscore.packet.ClientOpActFamilyFightScore;
import com.stars.modules.opactfamilyfightscore.packet.ServerOpActFamilyFightScore;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2017-03-21 10:27
 */
public class OpActFamilyFightScorePacketSet extends PacketSet {
    public static short S_OpActFamilyFightScore = 0x6214;//上行
    public static short C_OpActFamilyFightScore = 0x6215;//下行
    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ClientOpActFamilyFightScore.class);
        al.add(ServerOpActFamilyFightScore.class);
        return al;
    }
}
