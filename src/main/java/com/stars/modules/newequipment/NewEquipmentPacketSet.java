package com.stars.modules.newequipment;

import com.stars.modules.newequipment.packet.ClientNewEquipment;
import com.stars.modules.newequipment.packet.ServerNewEquipment;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * 装备相关的协议;
 * Created by wuyuxing on 2016/11/14.
 */
public class NewEquipmentPacketSet extends PacketSet {

    public static short S_NEW_EQUIPMENT = 0x0180;   //新装备系统协议上行
    public static short C_NEW_EQUIPMENT = 0x0181;   //新装备系统协议下行

    public NewEquipmentPacketSet() {

    }

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ServerNewEquipment.class);
        al.add(ClientNewEquipment.class);
        return al;
    }

}