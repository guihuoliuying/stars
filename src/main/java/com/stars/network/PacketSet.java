package com.stars.network;

import com.stars.network.server.packet.Packet;

import java.util.List;


public abstract class PacketSet {
    public abstract List<Class<? extends Packet>> getPacketList();
}
