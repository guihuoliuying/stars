package com.stars.server.login.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.server.login.LoginConstant;

import java.io.IOException;

public class ClientWarning extends Packet {

    private String key;
    private String[] params;

    public ClientWarning() {
    }

    public ClientWarning(String key) {
        this.key = key;
    }

    public ClientWarning(String key, String... params) {
        this.key = key;
        this.params = params;
    }

    @Override
    public short getType() {
        return LoginConstant.CLIENT_WARNING;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(0);
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

}
