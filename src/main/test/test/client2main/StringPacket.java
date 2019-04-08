package test.client2main;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.io.IOException;

/**
 * Created by zd on 2015/7/17.
 */
public class StringPacket extends Packet {

    String s = "failed";

    @Override
    public void execPacket() {
    }

    @Override
    public short getType() {
        return 0x6000;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        s = buff.readString();
    }

    @Override
    public byte[] toByteArray() throws IOException {
        return new byte[0];
    }

}
