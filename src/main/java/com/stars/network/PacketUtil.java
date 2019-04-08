package com.stars.network;

import com.stars.modules.demologin.packet.ClientText;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.network.server.session.GameSession;
import com.stars.util.LogUtil;
import io.netty.buffer.Unpooled;

public class PacketUtil {

    /**
     * 给客户端提示请求异常
     *
     * @param session
     */
    public static void error(GameSession session) {
        PacketManager.send(session, new ClientText(("请求异常")));
    }

    public static byte[] packetToBytes(Packet packet) {
        com.stars.network.server.buffer.NewByteBuffer buffer = new NewByteBuffer(Unpooled.buffer());
        byte[] bytes = null;
        try {
            packet.writeToBuffer(buffer);
            bytes = new byte[buffer.getBuff().readableBytes()];
            buffer.getBuff().readBytes(bytes);
        } catch (Throwable t) {
            LogUtil.info("packetToBytes|异常|packetType:" + packet.getType() + "|roleId:" + packet.getRoleId(), t);
        } finally {
            buffer.getBuff().release();
        }
        return bytes;
    }

}
