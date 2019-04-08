package com.stars.modules.newsignin;

import com.stars.modules.newsignin.packet.ClientRoleSignin;
import com.stars.modules.newsignin.packet.ClientSigninAward;
import com.stars.modules.newsignin.packet.ClientSigninVo;
import com.stars.modules.newsignin.packet.ServerNewSignin;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2017/2/5 17:10
 */
public class NewSigninPacketSet extends PacketSet {
    public static final short C_SignIn = 0x0164;        //响应
    public static final short C_SigninAward = 0x0165;   //下发签到获得的奖励
    public static final short C_SigninVo = 0x0166;      //下发产品数据
    public static final short S_SignIn = 0x0167;        //请求

    @Override
    public List<Class<? extends com.stars.network.server.packet.Packet>> getPacketList() {
        List<Class<? extends Packet>> al = new ArrayList<>();
        al.add(ServerNewSignin.class);
        al.add(ClientRoleSignin.class);
        al.add(ClientSigninAward.class);
        al.add(ClientSigninVo.class);
        return al;
    }
}
