package com.stars.modules.demologin.packet;

import com.stars.modules.demologin.LoginPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.io.IOException;

public class ClientText extends Packet {

	private int clientSystemConstant = 0;
	private String key;
	private String[] params;

	public ClientText() {
	}

	public ClientText(String key) {
		this.key = key;
	}

	public ClientText(String key, String... params) {
		this.key = key;
		this.params = params;
	}

	public ClientText(int clientSystemConstant, String key) {
		this.clientSystemConstant = clientSystemConstant;
		this.key = key;
	}

	public ClientText(int clientSystemConstant, String key, String... params){
		this.clientSystemConstant = clientSystemConstant;
		this.key = key;
		this.params = params;
	}

	@Override
	public short getType() {
		return LoginPacketSet.C_TEXT;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
		buff.writeInt(this.clientSystemConstant);
        buff.writeString(key);
        if (params != null && params.length > 0) {
            buff.writeByte((byte) params.length);
            for (String p : params) {
                buff.writeString(p);
            }
        } else {
            buff.writeByte((byte) 0);
        }
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {

	}

	@Override
	public byte[] toByteArray() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void execPacket() {
		// TODO Auto-generated method stub
		
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
