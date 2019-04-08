package com.stars.modules.quwudu.packet;

import com.stars.modules.quwudu.QuwuduPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * Created by huwenjun on 2017/5/18.
 */
public class ClientQuwuduPacket extends Packet {
    private Integer myTime;
    private Integer maxTime;

    public ClientQuwuduPacket(Integer myTime, Integer maxTime) {
        this.myTime = myTime;
        this.maxTime = maxTime;
    }

    public ClientQuwuduPacket() {
    }

    @Override
    public short getType() {
        return QuwuduPacketSet.C_QUWUDU;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(maxTime);
        buff.writeInt(myTime);
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }
}
