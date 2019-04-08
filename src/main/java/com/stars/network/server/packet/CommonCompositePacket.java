package com.stars.network.server.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.log.CoreLogger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zd on 2015/5/19.
 */
public class CommonCompositePacket extends com.stars.network.server.packet.Packet {

    private List<com.stars.network.server.packet.Packet> list = new LinkedList<>();

    @Override
    public short getType() {
        return 0x7812;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(list.size());
        for (com.stars.network.server.packet.Packet p : list) {
            buff.writeShort(p.getType());
            p.writeToBuffer(buff);
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        int size = buff.readInt();
        for (int i = 0; i < size; i++) {
            com.stars.network.server.packet.Packet packet = com.stars.network.server.packet.Packet.newPacket(buff, session);
            if (packet == null) {
                CoreLogger.error("CommonCompositePacket readFromBuffer read packet is null");
                return;
            }
            list.add(packet);
        }
    }

    @Override
    public byte[] toByteArray() throws IOException {
        return new byte[0];
    }

    @Override
    public void execPacket() {
        //do not call it
    }

    public void addPacket(com.stars.network.server.packet.Packet p) {
        list.add(p);
    }

    public List<Packet> getList() {
        return list;
    }

}
