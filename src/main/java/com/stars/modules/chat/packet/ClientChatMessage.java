package com.stars.modules.chat.packet;

import com.stars.modules.chat.ChatPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.services.chat.ChatMessage;

import java.util.ArrayList;

public class ClientChatMessage extends Packet {
	
	
	private byte[] data;
	
	private ChatMessage singleMessage;
	
	private ArrayList<ChatMessage>listMessage;
	
	public ClientChatMessage(){
		
	}
	
	public ClientChatMessage(byte data[]){
		this.data = data;
	}
	
	public ClientChatMessage(ChatMessage message) {
		this.singleMessage = message;
	}
	
	public ClientChatMessage(ArrayList<ChatMessage> list) {
		this.listMessage = list;
	}
	

	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return ChatPacketSet.Client_ChatMessage;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
//		System.err.println("*********************************");
		if (data != null) {
			buff.writeBytes(data);
		}else if (singleMessage != null) {
			buff.writeByte((byte)1);
			singleMessage.writeToBuffer(buff);
		}else {
			buff.writeByte((byte)listMessage.size());
			for (ChatMessage message : listMessage) {
				message.writeToBuffer(buff);
			}
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

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public ChatMessage getSingleMessage() {
		return singleMessage;
	}

	public void setSingleMessage(ChatMessage singleMessage) {
		this.singleMessage = singleMessage;
	}

	public ArrayList<ChatMessage> getListMessage() {
		return listMessage;
	}

	public void setListMessage(ArrayList<ChatMessage> listMessage) {
		this.listMessage = listMessage;
	}

}
