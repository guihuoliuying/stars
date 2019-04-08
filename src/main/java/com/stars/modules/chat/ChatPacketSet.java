package com.stars.modules.chat;

import com.stars.modules.chat.packet.ClientChatMessage;
import com.stars.modules.chat.packet.ClientRefuseChannel;
import com.stars.modules.chat.packet.ServerChatMessage;
import com.stars.modules.chat.packet.ServerRefuseChannel;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class ChatPacketSet extends PacketSet {
	
	public static short Client_ChatMessage = 0x0026;
	public static short Server_ChatMessage = 0x0027;
	public static short Server_RefuseChannel = 0x0028;
	public static short Client_RefuseChannel = 0x0029;

	@Override
	public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
		List<Class<? extends com.stars.network.server.packet.Packet>> al = new ArrayList<Class<? extends Packet>>();
		al.add(ClientChatMessage.class);
		al.add(ServerChatMessage.class);
		al.add(ServerRefuseChannel.class);
		al.add(ClientRefuseChannel.class);
		return al;
	}

}
