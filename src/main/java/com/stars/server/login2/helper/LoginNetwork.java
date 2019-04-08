package com.stars.server.login2.helper;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.server.login2.LoginServer2;
import com.stars.server.login2.model.pojo.LZoneServer;
import io.netty.channel.Channel;

import java.util.List;

/**
 * Created by zhaowenshuo on 2016/2/1.
 */
public class LoginNetwork {

    public static void succeed(Channel ch, String token, List<LZoneServer> zoneList) {
        NewByteBuffer buf = new NewByteBuffer(ch.alloc().buffer());
        buf.writeShort(com.stars.server.login2.LoginServer2.PROTO_CLIENT_LOGIN_CHECK);
        buf.writeByte((byte) 1); // 成功
        buf.writeString(token);
//        buf.writeString("127.0.0.1");
//        buf.writeInt(7090);
        // 大区列表
        buf.writeInt(zoneList.size());
        for (LZoneServer zone : zoneList) {
            buf.writeInt(zone.getId());
            buf.writeString(zone.getName());
            buf.writeString(zone.getIp());
            buf.writeInt(zone.getPort());
        }
        ch.writeAndFlush(buf.getBuff());
    }

    public static void fail(Channel ch, String errorMsg) {
        NewByteBuffer buf = new NewByteBuffer(ch.alloc().buffer());
        buf.writeShort(LoginServer2.PROTO_CLIENT_LOGIN_CHECK);
        buf.writeByte((byte) 0); // 失败
        buf.writeString(errorMsg);
        ch.writeAndFlush(buf.getBuff());
    }

}
