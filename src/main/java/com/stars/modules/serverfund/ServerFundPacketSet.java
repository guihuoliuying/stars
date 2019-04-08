package com.stars.modules.serverfund;

import com.stars.modules.serverfund.packet.ClientServerFund;
import com.stars.modules.serverfund.packet.ServerServerFund;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class ServerFundPacketSet extends PacketSet {
	//0x021B ~ 0x021E
	public static short S_SERVER_FUND = 0x021B;//请求
	public static short C_SERVER_FUND = 0x021E;//响应

	@Override
	public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
		List<Class<? extends com.stars.network.server.packet.Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ServerServerFund.class);
        al.add(ClientServerFund.class);
        return al;
	}

}
