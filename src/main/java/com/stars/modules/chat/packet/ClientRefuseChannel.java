package com.stars.modules.chat.packet;

import com.stars.modules.chat.ChatPacketSet;
import com.stars.modules.chat.prodata.ChatBanVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class ClientRefuseChannel extends Packet {
	
	private ArrayList<Byte>refuseChannel;
	private byte subType;
	List<ChatBanVo> chatBanVoList = new ArrayList();
	private int remainSecond;

	public void setRemainSecond(int remainSecond) {
		this.remainSecond = remainSecond;
	}

	public ClientRefuseChannel(){
		refuseChannel = new ArrayList<Byte>();
	}
	
	public void addRefuseChannel(byte channle){
		this.refuseChannel.add(channle);
	}

	public void setSubType(byte subType) {
		this.subType = subType;
	}

	public void setChatBanVoList(List<ChatBanVo> chatBanVoList) {
		this.chatBanVoList = chatBanVoList;
	}

	@Override
	public short getType() {
		return ChatPacketSet.Client_RefuseChannel;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
		buff.writeByte(subType);
		if (subType == (byte)0) {
			buff.writeByte((byte) refuseChannel.size());
			for (Byte byte1 : refuseChannel) {
				buff.writeByte(byte1);
			}
		}else if (subType == (byte)1){  //登陆下发产品数据
			buff.writeInt(chatBanVoList.size());
			for(int i = 0; i < chatBanVoList.size();i ++ ){
				ChatBanVo chatBanVo = chatBanVoList.get(i);
				buff.writeByte(chatBanVo.getChannel());
				buff.writeString(chatBanVo.getLevelSection());
				buff.writeString(chatBanVo.getVipSection());
				buff.writeInt(chatBanVo.getWordLimit());
				buff.writeInt(chatBanVo.getTimeLimit());
				buff.writeString(chatBanVo.getGeneralBan());
				buff.writeString(chatBanVo.getSilentBan());
			}
		}else if (subType == (byte)2){  //同步还剩下的禁言秒数
			buff.writeInt(remainSecond);
		}
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		// TODO Auto-generated method stub

	}

	@Override
	public void execPacket() {
		// TODO Auto-generated method stub

	}

}
