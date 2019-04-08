package com.stars.modules.demologin.packet;

import com.stars.modules.demologin.LoginPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.io.IOException;

public class ClientAnnouncement extends Packet {
	private int announcementType = 0;//目前全服公告只有一种，这里留着以后拓展用
	private String key;
	private String[] params;

	public ClientAnnouncement() {
	}

	public ClientAnnouncement(String key) {
		this.key = key;
	}

	public ClientAnnouncement(String key, String... params) {
		this.key = key;
		this.params = params;
	}

	public ClientAnnouncement(int announcementType, String key) {
		this.announcementType = announcementType;
		this.key = key;
	}

	public ClientAnnouncement(int announcementType, String key, String... params){
		this.announcementType = announcementType;
		this.key = key;
		this.params = params;
	}

	@Override
	public short getType() {
		return LoginPacketSet.C_ANNOUNCEMENT;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
		buff.writeInt(this.announcementType);
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
