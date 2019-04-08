package com.stars.modules.pk.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * @author dengzhou
 *连接服发到战斗服的，用于告诉战斗服哪个连接对应哪个玩家
 */
public class ConnectRegisterToFightServer extends Packet {

	
	private long fighter;
	
	public ConnectRegisterToFightServer(){
		
	}
	
	@Override
	public short getType() {
		return 0X7F16;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {

	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		fighter = buff.readLong();
	}

	@Override
	public void execPacket() {

	}

	public long getFighter() {
		return fighter;
	}

	public void setFighter(long fighter) {
		this.fighter = fighter;
	}

}
