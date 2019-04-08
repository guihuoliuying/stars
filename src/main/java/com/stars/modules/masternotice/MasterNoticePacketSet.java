package com.stars.modules.masternotice;

import com.stars.modules.masternotice.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaopeidian on 2016/11/16.
 */
public class MasterNoticePacketSet extends PacketSet {
	//C_MASTER_NOTICE_COUNT_DOWN 协议的Flag
	public static final byte Flag_Add_Refresh_Count = 0;
	public static final byte Flag_Begin_Count_Down = 1;
	
    public static short S_MASTER_PAGE_INFO = 0x0171;
    public static short C_MASTER_PAGE_INFO = 0x0172;
    
    public static short S_MASTER_NOTICE = 0x0173;
    public static short C_MASTER_NOTICE  = 0x0174;
    
    public static short S_MASTER_NOTICE_REFRESH = 0x0175;
    public static short C_MASTER_NOTICE_REFRESH  = 0x0176;
    
    public static short C_MASTER_NOTICE_COUNT_DOWN  = 0x0177;
    
    public MasterNoticePacketSet() {

    }

    @Override
    public List<Class<? extends Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ServerMasterNoticePageInfo.class);
        al.add(ClientMasterNoticePageInfo.class);
        al.add(ServerMasterNotice.class);
        al.add(ClientMasterNotice.class);
        al.add(ServerMasterNoticeRefresh.class);
        al.add(ClientMasterNoticeRefresh.class);
        al.add(ClientMasterNoticeCountDown.class);
        return al;
    }

}