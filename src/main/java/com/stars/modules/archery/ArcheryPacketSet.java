package com.stars.modules.archery;

import com.stars.modules.archery.packet.ClientArchery;
import com.stars.modules.archery.packet.ServerArchery;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author huzhipeng
 * 2017-06-08
 */
public class ArcheryPacketSet extends PacketSet{
	
	public static final short CLIENT_ARCHERY = 0x0288;
	public static final short SERVER_ARCHERY = 0x0289;

	@Override
	public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
		List<Class<? extends Packet>> list = new ArrayList<>();
		list.add(ClientArchery.class);
		list.add(ServerArchery.class);
		return list;
	}
	
}
