package com.stars.modules.everydaycharge.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.everydaycharge.EverydayChargeModule;
import com.stars.modules.everydaycharge.EverydayChargePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

public class ServerEverydayCharge extends PlayerPacket {

	public static final byte REQ_OPEN_UI = 1;//打开ui
	public static final byte REQ_LOTTERY = 2;//请求抽奖
	
	private byte reqType;
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		reqType = buff.readByte();
	}
	
	@Override
	public void execPacket(Player player) {
		EverydayChargeModule chargeModule = module(MConst.EverydayCharge);
		switch (reqType) {
		case REQ_OPEN_UI:
			chargeModule.openUI();
			break;
		case REQ_LOTTERY:
			chargeModule.getReward();
			break;
		default:
			break;
		}
	}

	@Override
	public short getType() {
		return EverydayChargePacketSet.S_EVERYDAY_CHARGE;
	}

}
