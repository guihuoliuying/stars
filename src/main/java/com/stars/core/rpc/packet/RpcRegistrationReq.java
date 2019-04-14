package com.stars.core.rpc.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Rpc客户端连接注册包（表示这个连接属于哪个服务器）
 * Created by zhaowenshuo on 2016/11/2.
 */
public class RpcRegistrationReq extends Packet {

    private List<Integer> serverIdList; // 服务器id

    @Override
    public short getType() {
        return 0x7F20;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(serverIdList.size());
        for (Integer serverId : serverIdList) {
            buff.writeInt(serverId);
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        int size = buff.readInt();
        serverIdList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            serverIdList.add(buff.readInt());
        }
    }

    @Override
    public void execPacket() {

    }

    public List<Integer> getServerIdList() {
        return serverIdList;
    }

    public void setServerIdList(List<Integer> serverIdList) {
        this.serverIdList = serverIdList;
    }
}
