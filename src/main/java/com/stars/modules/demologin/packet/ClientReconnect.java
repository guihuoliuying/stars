package com.stars.modules.demologin.packet;

import com.stars.modules.demologin.LoginPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.util.LogUtil;

/**
 * Created by wuyuxing on 2017/1/12.
 */
public class ClientReconnect extends Packet {

    private byte opType;
    private String token;
    private boolean success;
    private byte reason;
    private int maxServerPacketId;

    public ClientReconnect(boolean success) {
        this.success = success;
        this.opType = 1;
        this.reason = 0;
    }

    public ClientReconnect(String token) {
        this.token = token;
        this.opType = 0;
    }

    public ClientReconnect() {
    }

    @Override
    public short getType() {
        return LoginPacketSet.C_RECONNECT;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(opType);
        if (opType == 0) {
            buff.writeString(token);
        } else {
            buff.writeByte((byte) (success ? 1 : 0));
            buff.writeByte(reason);
            buff.writeInt(maxServerPacketId);
            LogUtil.info("发送ClientReconnect|maxServerPacketId:{}", maxServerPacketId);
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }

    public byte getReason() {
        return reason;
    }

    public void setReason(byte reason) {
        this.reason = reason;
    }

    public int getMaxServerPacketId() {
        return maxServerPacketId;
    }

    public void setMaxServerPacketId(int maxServerPacketId) {
        this.maxServerPacketId = maxServerPacketId;
    }
}
