package com.stars.modules.pk;

import com.stars.modules.pk.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class PKPacketSet extends PacketSet {
	
	public static short Client_PK_START = 0x0100;
	
	public static short Server_PK_Option = 0x0101;// pk操作请求
	
	public static short F2SPVPBACK = 0x0102;
	
	public static short Client_PVP_Data = 0x0103;
	
	public static short Server_PVP_Data = 0x0104;
	
	public static short Start_PVP_Request = 0x0105;
	
	public static short RegistConn_To_FightServer = 0x0106;
	
//	public static short Client_PK_Over = 0x0107;// 已废弃
	
	public static short PVP_RESULT = 0x0108;

	public static short Client_Player_Update = 0x0109;

	public static short Client_PK_Option = 0x010A;// pk操作响应

	@Override
	public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
		ArrayList<Class<? extends com.stars.network.server.packet.Packet>>al = new ArrayList<Class<? extends Packet>>();
		al.add(ClientPKStart.class);
		al.add(ClientPVPData.class);
		al.add(PVP1FightBackPacket.class);
		al.add(ServerPKOption.class);
		al.add(ServerPVPData.class);
		al.add(StartPVP1FightPacket.class);
		al.add(PVPResultPacket.class);
		al.add(ClientUpdatePlayer.class);
		al.add(ClientPKOption.class);
		return al;
	}

}
