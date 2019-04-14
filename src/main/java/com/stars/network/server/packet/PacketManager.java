package com.stars.network.server.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.session.GameSession;
import com.stars.network.server.session.SessionManager;
import com.stars.server.connector.packet.*;
import com.stars.util.log.CoreLogger;
import com.stars.core.rpc.packet.RpcInvocationReq;
import com.stars.core.rpc.packet.RpcRegistrationReq;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;

/**
 * Created by zd on 2015/3/13.
 */
public class PacketManager {

    private static Class packetClass[] = new Class[Short.MAX_VALUE];

    /**
     * 注册协议
     *
     * @param type
     * @param clazz
     * @throws Exception
     */
    static void register(short type, Class clazz) throws Exception {
        if (type < 0) {
            throw new IllegalArgumentException("type " + type + "超出Short范围");
        }
        if (clazz == null) {
            throw new IllegalArgumentException("class is null");
        }
        if (packetClass[type] != null) {
            throw new IllegalArgumentException("type " + type + "重复,class " + clazz);
        }
        Object packet = clazz.newInstance();
        if (packet instanceof com.stars.network.server.packet.Packet) {
            short t = ((com.stars.network.server.packet.Packet) packet).getType();
            if (t != type) {
                throw new IllegalArgumentException("type参数错误 " + type);
            } else {
                packetClass[type] = clazz;
            }
        } else {
            throw new IllegalArgumentException("class参数必须是Packet类型");
        }

    }

    /**
     * 注册业务层协议
     *
     * @param clazz
     * @throws Exception
     */
    public static void register(Class clazz) throws Exception {
        com.stars.network.server.packet.Packet packet = (com.stars.network.server.packet.Packet) clazz.newInstance();
//        if (isCorePacket(packet)) {
//            throw new IllegalArgumentException("0x7000 is reserved for core");
//        }
        register(packet.getType(), clazz);
    }

    /**
     * 获取协议包类型
     *
     * @param type
     * @return
     */
    public static Class get(short type) {
        if (type < 0) {
            throw new IllegalArgumentException("type " + type + "超出Short范围");
        }
        return packetClass[type];
    }

    /**
     * 根据session发送packet
     *
     * @param session
     * @param packet
     */
    public static void send(com.stars.network.server.session.GameSession session, com.stars.network.server.packet.Packet packet) {
        if (session == null) {
            com.stars.util.log.CoreLogger.error("会话为null, packetType={}, roleId={}",
                    String.format("0x%04X", packet.getType()), packet.getRoleId());
            return;
        }
        packet.setSession(session);
        serializeAndSend(session.getChannel(), packet); // 防止并发问题
    }

    /**
     * 根据roleId发送packet，如果roleId没有session直接返回
     *
     * @param roleId
     * @param packet
     */
    public static void send(long roleId, com.stars.network.server.packet.Packet packet) {
        com.stars.network.server.session.GameSession session = com.stars.network.server.session.SessionManager.getSessionMap().get(roleId);
        send(session, packet);
    }

    /**
     * 根据roleId发送packet，如果roleId没有session直接返回
     *
     * @param roleId
     * @param packet
     */
    public static void send(long roleId, short packetType, int packetId, byte[] bytes) {
        com.stars.network.server.session.GameSession session = com.stars.network.server.session.SessionManager.getSessionMap().get(roleId);
        if (session != null) {
            send(session.getChannel(), session.getConnectionId(), packetType, packetId, bytes);
        } else {
            com.stars.util.log.CoreLogger.error("会话为null, packetType={}, roleId={}",
                    String.format("0x%04X", packetType), roleId);
        }
    }

    
    public static void send(Channel channel, com.stars.network.server.packet.Packet packet){
    	serializeAndSend(channel, packet);
    }
    

    public static boolean isCorePacket(com.stars.network.server.packet.Packet packet) {
        return isCorePacket(packet.getType());
    }

    public static boolean isCorePacket(short type) {
        return (type & 0b01111111_00000000) == 0b01111111_00000000; // 0x7F00 ~ 0x7FFF 底层专用
    }

    public static void unbindBackend(com.stars.network.server.session.GameSession session) {
        com.stars.network.server.packet.Packet packet = new com.stars.server.connector.packet.UnbindBackendM2nPacket();
        packet.setSession(session);
        send(session, packet);
    }

    public static void closeFrontend(GameSession session) {
        com.stars.network.server.packet.Packet packet = new com.stars.server.connector.packet.CloseFrontendM2nPacket();
        packet.setSession(session);
        send(session, packet);
//        Packet p = new Disconnected(session.getRoleId());
//        p.setSession(session);
//        MainServer.getBusiness().dispatch(p);
        if (session.getRoleId() != 0) {
            SessionManager.remove(session.getRoleId(), session);
            com.stars.util.log.CoreLogger.error("channelInactive, roleId = " + session.getRoleId());
        } else {
            com.stars.util.log.CoreLogger.error("channelInactive, roleId = 0");
        }
    }

    private static void serializeAndSend(Channel channel, Packet packet) {
        ByteBuf buf = channel.alloc().buffer();
        try {
            // 包头
            buf.writeByte((byte) -82);
            // 消息长度占位
            buf.writeInt(-1);
            // 写入连接ID
            buf.writeInt(packet.getSession().getConnectionId());
            // 消息内容
            buf.writeShort(packet.getType());
            buf.writeInt(packet.getPacketId());
            packet.writeToBuffer(new NewByteBuffer(buf));
            // 包尾
            buf.writeByte((byte) -81);
            // 消息长度计算
            int msgLength = buf.readableBytes();
            int writerIndex = buf.writerIndex();
            buf.writerIndex(writerIndex - msgLength + 1);
            // 1 + 4 + 1 = 6
            buf.writeInt(msgLength - 6);
            buf.writerIndex(writerIndex);
            channel.writeAndFlush(buf);
        } catch (Exception e) {
            ReferenceCountUtil.release(buf);
            com.stars.util.log.CoreLogger.error("粘包错误: " + packet.getClass().getSimpleName(), e);
            throw e;
        }

    }

    private static void send(Channel channel, int connectionId, short packetType, int packetId, byte[] bytes) {
        ByteBuf buf = channel.alloc().buffer();
        try {
            // 包头
            buf.writeByte((byte) -82);
            // 消息长度占位
            buf.writeInt(-1);
            // 写入连接ID
            buf.writeInt(connectionId);
            // 消息内容
            buf.writeShort(packetType);
            buf.writeInt(packetId); // packetId
            buf.writeBytes(bytes);
            // 包尾
            buf.writeByte((byte) -81);
            // 消息长度计算
            int msgLength = buf.readableBytes();
            int writerIndex = buf.writerIndex();
            buf.writerIndex(writerIndex - msgLength + 1);
            // 1 + 4 + 1 = 6
            buf.writeInt(msgLength - 6);
            buf.writerIndex(writerIndex);
            channel.writeAndFlush(buf);
        } catch (Exception e) {
            ReferenceCountUtil.release(buf);
            CoreLogger.error("粘包错误: " + packetType, e);
            throw e;
        }
    }

    /**
     * 加载底层协议包
     *
     * @throws Exception
     */
    public static void loadCorePacket() throws Exception {

        register((short) 0x7F10, CloseFrontendM2nPacket.class); // 关闭前端连接
        register((short) 0x7F11, FrontendClosedN2mPacket.class); // 前端连接断开
        register((short) 0x7F12, UnbindBackendM2nPacket.class); // 解除绑定后端连接
        register((short) 0x7F13, PingN2mPacket.class); // 心跳测试
        register((short) 0X7F14, PongM2nPacket.class); // 心跳响应
//        register((short) 0x7F15, MonitorReq.class);
//        register((short) 0x7F16, MonitorRsp.class);
        register((short) 0x7F20, RpcRegistrationReq.class);
        register((short) 0x7F21, RpcInvocationReq.class);
    }

}
