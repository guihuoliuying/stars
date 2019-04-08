package com.stars.modules.tecentvideo;

import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaopeidian on 2017/5/8.
 */
public class TencentVideoPacketSet extends PacketSet {
    public TencentVideoPacketSet() {

    }

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<Class<? extends Packet>>();
        return al;
    }
}