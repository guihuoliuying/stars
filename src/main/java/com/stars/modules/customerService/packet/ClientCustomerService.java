package com.stars.modules.customerService.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.customerService.CustomerServicePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

public class ClientCustomerService extends PlayerPacket {
	
	private byte result;

	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeByte(result);// 0 失败        1 成功         2 已经记录过
	}

	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return CustomerServicePacketSet.Client_CustomerService;
	}

	public byte getResult() {
		return result;
	}

	public void setResult(byte result) {
		this.result = result;
	}

}
