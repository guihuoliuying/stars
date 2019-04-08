package com.stars.network;

import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaowenshuo on 2016/10/9.
 */
public class PlaceholderPacketSet extends PacketSet {
    @Override
    public List<Class<? extends Packet>> getPacketList() {
        return new ArrayList<>();
    }
}
