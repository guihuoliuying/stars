package com.stars.modules.everydaycharge;

import com.stars.modules.everydaycharge.packet.ClientEverydayCharge;
import com.stars.modules.everydaycharge.packet.ServerEverydayCharge;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class EverydayChargePacketSet extends PacketSet {
	// (short) 0x021F, (short) 0x0221
	public static short S_EVERYDAY_CHARGE = 0x021F;// 请求
	public static short C_EVERYDAY_CHARGE = 0x0220;// 响应

	@Override
	public List<Class<? extends Packet>> getPacketList() {
		List<Class<? extends Packet>> al = new ArrayList<Class<? extends Packet>>();
		al.add(ServerEverydayCharge.class);
		al.add(ClientEverydayCharge.class);
		return al;
	}
}
