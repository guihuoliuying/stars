package com.stars.modules.daily5v5.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.daily5v5.Daily5v5Module;
import com.stars.modules.daily5v5.Daily5v5PacketSet;
import com.stars.multiserver.daily5v5.Daily5v5Manager;
import com.stars.network.server.buffer.NewByteBuffer;

public class ServerDaily5v5 extends PlayerPacket {
	
	private byte opType;

	@Override
	public void execPacket(Player player) {
		Daily5v5Module module = module(MConst.Daily5v5);
		if(opType== Daily5v5Manager.OPEN_UI){
			module.openUI();
		}else if(opType==Daily5v5Manager.START_MATCHING){
			module.startMatching();
		}else if(opType==Daily5v5Manager.CANCEL_MATCHING){
			module.cancleMatching(false);
		}else if(opType==Daily5v5Manager.YES_FIGHTING){
			module.continueFighting();
		}else if(opType==Daily5v5Manager.REQ_JOIN_TIMES){
			module.getJoinTimes();
		}
	}

	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return Daily5v5PacketSet.SERVER_DAILY_5V5;
	}
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		this.opType = buff.readByte();
	}
	
	public byte getOpType() {
		return opType;
	}

	public void setOpType(byte opType) {
		this.opType = opType;
	}

}
