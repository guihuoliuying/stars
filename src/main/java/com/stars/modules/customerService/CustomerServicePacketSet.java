package com.stars.modules.customerService;

import com.stars.modules.customerService.packet.ClientCustomerService;
import com.stars.modules.customerService.packet.ServerCustomerService;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class CustomerServicePacketSet extends PacketSet{
	
	public static final short Server_CustomerService = 0x02A8;
	public static final short Client_CustomerService = 0x02A9;

	@Override
	public List<Class<? extends Packet>> getPacketList() {
		List<Class<? extends Packet>> list = new ArrayList<>();
		list.add(ServerCustomerService.class);
		list.add(ClientCustomerService.class);
		return list;
	}

}
