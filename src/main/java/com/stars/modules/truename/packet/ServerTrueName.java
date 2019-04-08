package com.stars.modules.truename.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.truename.TrueNameModule;
import com.stars.modules.truename.TrueNamePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

public class ServerTrueName extends PlayerPacket {
	
	private String name;
	
	private String idNum;//identity card number
	
	private byte cardType;//1:身份证,2:港澳通行证,3:港澳台身份证,4:护照,5:军警证

	@Override
	public void execPacket(Player player) {
		TrueNameModule module = module(MConst.TrueName);
		module.saveMyInfo(this);
	}

	@Override
	public short getType() {
		return TrueNamePacketSet.SERVER_TRUE_NAME;
	}
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		this.name = buff.readString();
		this.idNum = buff.readString();
		this.cardType = buff.readByte();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdNum() {
		return idNum;
	}

	public void setIdNum(String idNum) {
		this.idNum = idNum;
	}

	public byte getCardType() {
		return cardType;
	}

	public void setCardType(byte cardType) {
		this.cardType = cardType;
	}

}
