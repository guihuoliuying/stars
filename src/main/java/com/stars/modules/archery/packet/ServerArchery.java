package com.stars.modules.archery.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.archery.ArcheryManager;
import com.stars.modules.archery.ArcheryModule;
import com.stars.modules.archery.ArcheryPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * @author huzhipeng
 * 2017-06-08
 */
public class ServerArchery extends PlayerPacket {

	private byte opType;
	
	private int integral;

	@Override
	public short getType() {
		return ArcheryPacketSet.SERVER_ARCHERY;
	}
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		this.opType = buff.readByte();
		if(opType==ArcheryManager.SYN_INTEGRAL){
			integral = buff.readInt();
		}else if(opType==ArcheryManager.GET_AWARD){
			integral = buff.readInt();
		}
	}
	
	@Override
	public void execPacket(Player player) {
		ArcheryModule archeryModule =  module(MConst.Archery);
		archeryModule.handleRequest(this);
	}

	public byte getOpType() {
		return opType;
	}

	public void setOpType(byte opType) {
		this.opType = opType;
	}

	public int getIntegral() {
		return integral;
	}

	public void setIntegral(int integral) {
		this.integral = integral;
	}

}
