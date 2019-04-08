package com.stars.modules.countDown;

import com.stars.modules.countDown.packet.ClientCountDown;
import com.stars.modules.countDown.packet.ServerCountDown;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author huzhipeng
 * 2017-06-08
 */
public class CountDownPacketSet extends PacketSet{
	
	public static final short CLIENT_COUNTDOWN = 0x02AD;
	public static final short SERVER_COUNTDOWN = 0x02AE;

	@Override
	public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
		List<Class<? extends Packet>> list = new ArrayList<>();
		list.add(ClientCountDown.class);
		list.add(ServerCountDown.class);
		return list;
	}
	
}
