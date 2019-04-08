package com.stars.modules.daily5v5;

import com.stars.modules.daily5v5.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class Daily5v5PacketSet extends PacketSet{
	
	public static final short SERVER_DAILY_5V5 = 0x6240;
	public static final short CLIENT_DAILY_5V5 = 0x6232;
	
	//战斗相关
	public static final short Client_Daily5v5PersonalPoint = 0x6233;
	public static final short Client_Daily5v5BattleStat = 0x6234;
	public static final short Client_Daily5v5Revive = 0x6235;
	public static final short Client_Daily5v5KillCount = 0x6236;
	public static final short Client_Daily5v5TeamPoints = 0x6237;
	public static final short Client_Daily5v5Morale = 0x6238;
	public static final short Server_Daily5v5Revive = 0x6239;
	public static final short Client_Daily5v5FightInitInfo = 0x623a;
	public static final short Client_Daily5v5FightUpdateInfo = 0x623b;
	public static final short Server_Daily5v5UseBuff = 0x623c;

	@Override
	public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
		List<Class<? extends Packet>> list = new ArrayList<>();
		list.add(ServerDaily5v5.class);
		list.add(ClientDaily5v5.class);
		list.add(ClientDaily5v5PersonalPoint.class);
		list.add(ClientDaily5v5BattleStat.class);
		list.add(ClientDaily5v5Revive.class);
		list.add(ClientDaily5v5KillCount.class);
		list.add(ClientDaily5v5TeamPoints.class);
		list.add(ClientDaily5v5Morale.class);
		list.add(ServerDaily5v5Revive.class);
		list.add(ClientDaily5v5FightInitInfo.class);
		list.add(ClientDaily5v5FightUpdateInfo.class);
		list.add(ServerDaily5v5UseBuff.class);
		return list;
	}

}
