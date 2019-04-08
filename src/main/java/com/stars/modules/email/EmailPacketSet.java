package com.stars.modules.email;

import com.stars.modules.email.packet.ClientEmail;
import com.stars.modules.email.packet.ClientRequestSendEmailBack;
import com.stars.modules.email.packet.ServerEmail;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaowenshuo on 2016/8/2.
 */
public class EmailPacketSet extends PacketSet {

    public static short S_EMAIL = 0x6000;// 邮件上行
    public static short C_EMAIL = 0x6001;// 邮件下行
    public static short C_REQUEST_SEND_EMAIL = 0x6002;// 由其他服务器下发请求发送邮件的协议;

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> list = new ArrayList<>();
        list.add(ServerEmail.class);
        list.add(ClientEmail.class);
        list.add(ClientRequestSendEmailBack.class);
        return list;
    }
}
