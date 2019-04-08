package com.stars.server.connector.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.io.IOException;

/**
 * Created by zws on 2015/8/28.
 */
public class PongM2nPacket extends Packet {

    @Override
    public short getType() {
        return 0X7F14;
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
