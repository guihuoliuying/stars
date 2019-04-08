package com.stars.modules.gameboard;

import com.stars.modules.gameboard.packet.ClientGameboard;
import com.stars.modules.gameboard.packet.ServerGameboard;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2017/1/5 18:35
 */
public class GameboardPacketSet extends PacketSet {
    public static final short C_GAMEBOARD = 0x01DC;
    public static final short S_GAMEBOARD = 0x01DD;
    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ClientGameboard.class);
        al.add(ServerGameboard.class);
        return al;
    }
}
