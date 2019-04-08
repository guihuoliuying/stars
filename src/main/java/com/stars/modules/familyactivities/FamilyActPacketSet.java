package com.stars.modules.familyactivities;

import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.List;

/**
 * Created by zhaowenshuo on 2016/10/8.
 */
public class FamilyActPacketSet extends PacketSet {

    /* 家族远征(0x6068 - 0x606F) */
    /* 家族篝火(0x6070 - 0x6077) */
    /* 家族入侵(0x6078 - 0x6080) */

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        return null;
    }
}
