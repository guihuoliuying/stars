package com.stars.modules.opactkickback;

import com.stars.modules.opactkickback.packet.ClientOpActKickBack;
import com.stars.modules.opactkickback.packet.ServerOpActKickBack;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class OpActKickBackPacketSet extends PacketSet {
	public static short S_OpActKickBack = 0x622F;
	public static short C_OpActKickBack = 0x6230;

	@Override
	public List<Class<? extends Packet>> getPacketList() {
		List<Class<? extends Packet>> al = new ArrayList<>();
		al.add(ClientOpActKickBack.class);
		al.add(ServerOpActKickBack.class);
		return al;
	}

}
