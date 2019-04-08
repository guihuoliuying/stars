package com.stars.modules.runeDungeon;

import com.stars.modules.runeDungeon.packet.ClientRuneDungeon;
import com.stars.modules.runeDungeon.packet.ServerRuneDungeon;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class RuneDungeonPacketSet extends PacketSet{
	
	public static final short Client_RuneDungeon = 0x02AA;
	public static final short Server_RuneDungeon = 0x02AB;

	@Override
	public List<Class<? extends Packet>> getPacketList() {
		List<Class<? extends Packet>> list = new ArrayList<>();
		list.add(ClientRuneDungeon.class);
		list.add(ServerRuneDungeon.class);
		return list;
	}

}
