package com.stars.server.connector.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.io.IOException;

/**
 * Created by zws on 2015/8/27.
 */
public class CloseFrontendM2nPacket extends Packet {

    @Override
    public short getType() {
        return 0x7F10;
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
