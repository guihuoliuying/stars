package com.stars.modules.customerService.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.customerService.CustomerServicePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

public class ServerCustomerService extends PlayerPacket {
	
	private String cellphone;
	private String qq;

	@Override
	public void execPacket(Player player) {

    }
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		cellphone = buff.readString();
		qq = buff.readString();
	}

	@Override
	public short getType() {
		return CustomerServicePacketSet.Server_CustomerService;
	}

}
