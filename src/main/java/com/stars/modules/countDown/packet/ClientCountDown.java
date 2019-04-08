package com.stars.modules.countDown.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.countDown.CountDownPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

/**
 * @author zhanghaizhen
 * 2017-06-29
 */
public class ClientCountDown extends PlayerPacket {
	
	public ClientCountDown() {
		// TODO Auto-generated constructor stub
	}
	
	public ClientCountDown(byte opType) {
		this.opType = opType;
	}
	
	private byte opType;

	
	private Map<Integer, Integer> award;
	
	@Override
	public short getType() {
		return CountDownPacketSet.CLIENT_COUNTDOWN;
	}
	
	@Override
	public void writeToBuffer(NewByteBuffer buff) {

	}
	
	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		
	}

	public byte getOpType() {
		return opType;
	}

	public void setOpType(byte opType) {
		this.opType = opType;
	}



}
