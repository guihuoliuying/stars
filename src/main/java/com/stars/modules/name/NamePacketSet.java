package com.stars.modules.name;

import com.stars.modules.name.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class NamePacketSet extends PacketSet {

    public static short Client_Random_Name = 0x0020;//随机名字

    public static short Server_Naming = 0x0021;//取名

    public static short Server_Req_Name = 0x0022;//请求一个随机名字
    public static final short S_RENAME = 0x0023;//上行重命名
    public static final short C_RENAME = 0x0024;//下行重命名

    public NamePacketSet() {
    }

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends com.stars.network.server.packet.Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ClientRandomName.class);
        al.add(ServerNaming.class);
        al.add(ServerRandomName.class);
        al.add(ServerRenamePacket.class);
        al.add(ClientRenamePacket.class);
        return al;
    }

}
