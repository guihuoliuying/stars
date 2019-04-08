package com.stars.modules.demologin;

import com.stars.modules.demologin.packet.*;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyuheng on 2016/6/16.
 */
public class LoginPacketSet extends PacketSet {

    /**创建角色;*/
    public static short C_CREATE_ROLE = 0x000B;
    /**下发帐号角色列表;*/
    public static short C_ACCOUNTROLELIST = 0x000A;
	public static short C_TEXT = 0x0008;
	public static short C_ANNOUNCEMENT = 0x0009;
    public static short S_DEMO_LOGIN = 0x000E;
    public static short C_DEMO_LOGIN = 0x000F;

    public static short S_REGISTER = 0x0000;
    public static short C_REGISTER = 0x0001;


    public static short S_LOGOUT = 0x0002;
    public static short C_LOGOUT = 0x0003;

    public static short S_RECONNECT = 0x0004;
    public static short C_RECONNECT = 0x0005;
    public static short C_BLOCK_ACCOUNT = 0x0006;// 封号提示

    public static short C_SERVER_DATE = 0x0007;//开服时间
    public static short C_PATCH = 0x000C; // 客户端数据补丁

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends com.stars.network.server.packet.Packet>> al = new ArrayList<Class<? extends Packet>>();
        al.add(ServerRoleOp.class);
        al.add(ClientAccountRoleList.class);
        al.add(ClientText.class);
        al.add(ClientAnnouncement.class);
        al.add(ServerLogin.class);
        al.add(ClientLogin.class);
        al.add(ServerRegister.class);
        al.add(ClientRegister.class);
        al.add(ServerLogout.class);
        al.add(ClientLogout.class);
        al.add(ServerReconnect.class);
        al.add(ClientReconnect.class);
        al.add(ClientBlockAccount.class);
        al.add(ClientServerDate.class);
        al.add(ClientPatch.class);
        return al;
    }
}
