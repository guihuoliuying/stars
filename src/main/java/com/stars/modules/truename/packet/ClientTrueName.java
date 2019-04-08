package com.stars.modules.truename.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.truename.TrueNamePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

public class ClientTrueName extends PlayerPacket {
	
	public ClientTrueName() {
		// TODO Auto-generated constructor stub
	}
	
	public ClientTrueName(byte opType) {
		this.opType = opType;
	}
	
	private byte opType;
	
	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public short getType() {
		return TrueNamePacketSet.CLIENT_TRUE_NAME;
	}
	
	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeByte(opType);
	}

	public byte getOpType() {
		return opType;
	}

	public void setOpType(byte opType) {
		this.opType = opType;
	}

}
