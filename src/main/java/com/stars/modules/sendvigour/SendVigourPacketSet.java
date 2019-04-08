package com.stars.modules.sendvigour;

import com.stars.modules.sendvigour.packet.ClientSendVigourData;
import com.stars.modules.sendvigour.packet.ServerGetVigourReward;
import com.stars.modules.sendvigour.packet.ServerSendVigourData;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaopeidian on 2017/3/29.
 */
public class SendVigourPacketSet extends PacketSet {
    public static final short S_SEND_VIGOUR_DATA = 0x0222;
    public static final short C_SEND_VIGOUR_DATA = 0x0223;
    public static final short S_GET_VIGOUR_REWARD = 0x0224;

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerSendVigourData.class);
        list.add(ClientSendVigourData.class);
        list.add(ServerGetVigourReward.class);
        return list;
    }
}
