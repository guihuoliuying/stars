package com.stars.modules.system;

import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zd on 2016/1/22.
 */
public class SystemPacketSet extends PacketSet {

    public SystemPacketSet() {
//        reg(TransferPacket.class);
//        reg(RouteToClientPacket.class);
//        reg(CloseFrontendM2nPacket.class);
//        reg(FrontendClosedN2mPacket.class);
//        reg(UnbindBackendM2nPacket.class);
//        reg(PingN2mPacket.class);
//        reg(PongM2nPacket.class);
    }

    @Override
	public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
		// TODO Auto-generated method stub
		List<Class<? extends com.stars.network.server.packet.Packet>> al = new ArrayList<Class<? extends Packet>>();
		return al;
	}
}
