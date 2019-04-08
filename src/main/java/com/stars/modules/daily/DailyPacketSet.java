package com.stars.modules.daily;

import com.stars.modules.daily.packet.ClientDailyData;
import com.stars.modules.daily.packet.ServerDailyData;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class DailyPacketSet extends PacketSet {
	
	public static short Client_DailyData = 0x002a;//下行数据
	public static short Server_DailyData = 0x002b;//上行数据

	@Override
	public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
		List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ClientDailyData.class);
        al.add(ServerDailyData.class);
        return al;
	}

}
