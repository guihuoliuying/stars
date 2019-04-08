package com.stars.modules.countDown.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.countDown.CountDownPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * @author zhanghaizhen
 * 2017-06-29
 */
public class ServerCountDown extends PlayerPacket {

	private byte opType;


	@Override
	public short getType() {
		return CountDownPacketSet.SERVER_COUNTDOWN;
	}
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {

	}
	
	@Override
	public void execPacket(Player player) {

	}

	public byte getOpType() {
		return opType;
	}

	public void setOpType(byte opType) {
		this.opType = opType;
	}



}
