package com.stars.server.connector.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.server.connector.Connector;

import java.io.IOException;

/**
 * Created by zws on 2015/8/27.
 */
public class FrontendClosedN2mPacket extends Packet {

    private int connectionId;

    public FrontendClosedN2mPacket() {

    }

    public FrontendClosedN2mPacket(int connectionId) {
        this.connectionId = connectionId;
    }

    public void connectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public short getType() {
        return 0x7F11;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buf) {
        buf.writeByte((byte) -82);
        buf.writeInt(4 + 2 + 4);
        buf.writeInt(connectionId);
        buf.writeShort(Connector.PROTO_FRONTEND_CLOSED);
        buf.writeInt(0);
        buf.writeByte((byte) -81);
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
