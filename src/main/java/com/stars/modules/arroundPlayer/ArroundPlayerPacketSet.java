package com.stars.modules.arroundPlayer;

import com.stars.modules.arroundPlayer.Packet.ClientArroundPlayer;
import com.stars.modules.arroundPlayer.Packet.ClientHeartbeat;
import com.stars.modules.arroundPlayer.Packet.ServerHeartbeat;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class ArroundPlayerPacketSet extends PacketSet {
	
	public static short Client_ArroundPlayer = 0x002e;//下发周围玩家
	public static short Server_Heartbeat = 0x002f;//心跳
	public static short Client_Heartbeat = 0x0030;//响应心跳

	@Override
	public List<Class<? extends Packet>> getPacketList() {
		List<Class<? extends Packet>> al = new ArrayList<Class<? extends Packet>>();
		al.add(ClientArroundPlayer.class);
		al.add(ServerHeartbeat.class);
		al.add(ClientHeartbeat.class);
		return al;
	}

}
