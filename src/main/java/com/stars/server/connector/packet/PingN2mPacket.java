package com.stars.server.connector.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.server.connector.Connector;

import java.io.IOException;

/**
 * Created by zws on 2015/8/28.
 */
public class PingN2mPacket extends Packet {

    @Override
    public short getType() {
        return 0x7F13;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buf) {
        buf.writeByte((byte) -82);
        buf.writeInt(6);
        buf.writeInt(-1); // non client-side packet(packet for channel)
        buf.writeShort(Connector.PROTO_PING);
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
