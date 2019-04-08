package com.stars.server.main.message;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.io.IOException;

public class Disconnected extends Packet {
	
	public Disconnected(){}

    public Disconnected(long roleId) {
        this.setRoleId(roleId);
    }

    @Override
    public short getType() {
        return 0;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public byte[] toByteArray() throws IOException {
        return new byte[0];
    }

    @Override
    public void execPacket() {

    }
}
