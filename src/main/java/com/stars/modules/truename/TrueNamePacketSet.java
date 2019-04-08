package com.stars.modules.truename;

import com.stars.modules.truename.packet.ClientTrueName;
import com.stars.modules.truename.packet.ServerTrueName;
import com.stars.modules.truename.packet.ServerTrueNameLogin;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class TrueNamePacketSet extends PacketSet{
	
	public static short CLIENT_TRUE_NAME = 0x028E;
	public static short SERVER_TRUE_NAME = 0x028F;
	public static short SERVER_TRUENAME_LOGIN = 0x0290;
	
	@Override
	public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
		List<Class<? extends Packet>> list = new ArrayList<>();
		list.add(ClientTrueName.class);
		list.add(ServerTrueName.class);
		list.add(ServerTrueNameLogin.class);
		return list;
	}

}
