package com.stars.modules.trump;

import com.stars.modules.trump.packet.ClientTrump;
import com.stars.modules.trump.packet.ClientTrumpKarmaPacket;
import com.stars.modules.trump.packet.ServerTrump;
import com.stars.modules.trump.packet.ServerTrumpKarmaPacket;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouyaohui on 2016/9/18.
 */
public class TrumpPacketSet extends PacketSet {
    /** 常量 */
    public final static short SERVER_TRUMP = 0x0130;
    public final static short CLIENT_TRUMP = 0x0131;
    public final static short SERVER_TRUMP_KARMA = 0x0132;
    public final static short CLIENT_TRUMP_KARMA = 0x0133;


    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> packetList = new ArrayList<>();
        packetList.add(ServerTrump.class);
        packetList.add(ClientTrump.class);
        packetList.add(ServerTrumpKarmaPacket.class);
        packetList.add(ClientTrumpKarmaPacket.class);
        return packetList;
    }
}
