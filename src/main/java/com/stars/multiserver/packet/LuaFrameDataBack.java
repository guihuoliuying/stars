package com.stars.multiserver.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

public class LuaFrameDataBack extends Packet {
	
	
	private String key;
	
	private String jData;

	public LuaFrameDataBack(){
		
	}
	
	public LuaFrameDataBack(String jData){
		this.jData = jData;
	}

	@Override
	public short getType() {
		return PacketDefine.LUA_FRAM_DATA;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeString(key);
		buff.writeString(jData);
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		this.key = buff.readString();
		this.jData = buff.readString();
	}

	@Override
	public void execPacket() {

	}

	public String getjData() {
		return jData;
	}

	public void setjData(String jData) {
		this.jData = jData;
	}


	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
