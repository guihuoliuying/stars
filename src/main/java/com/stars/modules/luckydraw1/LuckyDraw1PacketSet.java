package com.stars.modules.luckydraw1;

import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyDraw1PacketSet extends  PacketSet {
    @Override
    public List<Class<? extends Packet>> getPacketList() {
        return new ArrayList<>();
    }
}
