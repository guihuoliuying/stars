package com.stars.modules.relationoperation;

import com.stars.modules.relationoperation.packet.ClientRelationOperation;
import com.stars.modules.relationoperation.packet.ServerRelationOperation;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaowenshuo on 2016/9/14.
 */
public class RelationOperationPacketSet extends PacketSet {

    public static final short S_RELATION_OPERATION = 0x0110; // 请求
    public static final short C_RELATION_OPERATION = 0x0111; // 响应

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerRelationOperation.class);
        list.add(ClientRelationOperation.class);
        return list;
    }
}
