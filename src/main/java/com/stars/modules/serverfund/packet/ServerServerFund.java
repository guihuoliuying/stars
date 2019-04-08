package com.stars.modules.serverfund.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.serverfund.ServerFundModule;
import com.stars.modules.serverfund.ServerFundPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

public class ServerServerFund extends PlayerPacket {
	
	public final static byte REQ_OPENUI = 1;	//打开ui
	public final static byte REQ_BUY = 2;	//购买成功
	public final static byte REQ_GET = 3;	//领取成功

	private byte reqType;
	private int fundId;
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		reqType = buff.readByte();
		if (reqType == REQ_GET) {
			fundId = buff.readInt();
		}
	}
	
	@Override
	public void execPacket(Player player) {
		ServerFundModule fundModule = module(MConst.ServerFund);
		switch (reqType) {
		case REQ_OPENUI:
			fundModule.openFundUI();
			break;
		case REQ_BUY:
			fundModule.buyFund();
			break;
		case REQ_GET:
			fundModule.getFund(fundId);
			break;
		default:
			break;
		}
	}

	@Override
	public short getType() {
		return ServerFundPacketSet.S_SERVER_FUND;
	}

	public byte getReqType() {
		return reqType;
	}

	public void setReqType(byte reqType) {
		this.reqType = reqType;
	}

}
