package com.stars.modules.pk.packet;

import com.stars.modules.pk.PKPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * Created by daiyaorong on 2016/9/2.
 */
public class ClientPVPData extends Packet {

    private byte[] serverOrder;//服务端指令

    public ClientPVPData() {
    }

    @Override
    public short getType() {
        return PKPacketSet.Client_PVP_Data;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(this.serverOrder.length);
        buff.writeBytes(this.serverOrder);
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }

    public byte[] getServerOrder() {
        return serverOrder;
    }

    public void setServerOrder(byte[] serverOrder) {
        this.serverOrder = serverOrder;
    }

    public void addServerOrder(byte[] serverOrder) {
        if (serverOrder == null || serverOrder.length == 0 ) {
            return;
        }
        int currentLen = this.serverOrder.length;
        int newLen = serverOrder.length;
        byte[] totalOrder = new byte[currentLen+newLen];
        System.arraycopy(this.serverOrder, 0, totalOrder, 0, currentLen);
        System.arraycopy(serverOrder, 0, totalOrder, currentLen, newLen);
        this.serverOrder = totalOrder;
    }
}
