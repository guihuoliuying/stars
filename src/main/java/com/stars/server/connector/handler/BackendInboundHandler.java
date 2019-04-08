package com.stars.server.connector.handler;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.server.connector.BackendAddress;
import com.stars.server.connector.packet.PingN2mPacket;
import com.stars.server.connector.runnable.connection.BackendChannelBuildTask;
import com.stars.server.connector.runnable.connection.BackendChannelRebuildTask;
import com.stars.util.LogUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultChannelPromise;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.ScheduledFuture;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

/**
 * Created by zws on 2015/8/21.
 */
public class BackendInboundHandler extends ChannelInboundHandlerAdapter {

    private int serverId;
    private com.stars.server.connector.BackendAddress address;
    private ScheduledFuture future;
    private long timestamp;

    public BackendInboundHandler(int serverId, com.stars.server.connector.BackendAddress address) {
        this.serverId = serverId;
        this.address = address;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        timestamp = System.currentTimeMillis();
        // heartbeat at fixed rate
        future = ctx.channel().eventLoop().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // check heartbeat's timestamp
//                Channel ch = Connector.backendChannels.get()[serverId].channel();
                com.stars.server.connector.BackendSession backendSession = com.stars.server.connector.Connector.getBackendSession(serverId);
                if (backendSession != null) {
                    Channel ch = backendSession.channel();
                    if (System.currentTimeMillis() - timestamp > com.stars.server.connector.Connector.config.backendTimeout() * 1000) {
                    	com.stars.util.LogUtil.info("游戏服心跳超时, serverId={}, tid={}",
                                serverId, Thread.currentThread().getId());
                        ch.close();
                    } else {
                        com.stars.server.connector.ConnectorUtil.send(ch, new PingN2mPacket());
                    }
                }

            }
        }, 0, 5, TimeUnit.SECONDS);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        com.stars.util.LogUtil.info("游戏服连接断开, serverId={}, tid={}", serverId, Thread.currentThread().getId());
        if (!future.cancel(true)) {
            com.stars.util.LogUtil.info("定时任务取消失败, serverId={}, tid={}", serverId, Thread.currentThread().getId());
        }
        boolean isMainServer = serverId == com.stars.server.connector.Connector.MAIN_SERVER_ID;
        Iterator<com.stars.server.connector.FrontendSession> it = com.stars.server.connector.Connector.idToSession().values().iterator();
        while (it.hasNext()) {
            com.stars.server.connector.FrontendSession session = it.next();
            if (session.backendChannelIndex() == serverId) {
                if (isMainServer) { // 如果是主服，则关闭前端连接
                    it.remove();
                    session.frontendChannel().close();
                } else { // 非主服，则绑定回主服
                    com.stars.server.connector.handler.FrontendInboundHandler.bindBackend(session, com.stars.server.connector.Connector.MAIN_SERVER_ID, session.account());
                }
            }
        }
        // 重置后端会话
        com.stars.server.connector.Connector.setBackendSession(serverId, null);
        com.stars.util.LogUtil.info(com.stars.server.connector.Connector.backendSessions.get().toString());
        ctx.executor().schedule(
                new BackendChannelRebuildTask(serverId, address, ctx.channel().eventLoop())
                , 1, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        try {
            final int connectionId = buf.readInt();
            com.stars.server.connector.FrontendSession session = com.stars.server.connector.Connector.getSession(connectionId);
            short packetType = buf.getShort(buf.readerIndex());
            byte subtype = buf.readableBytes() >= 7 ? buf.getByte(6) : (byte) 0xFF;
            if (!PacketManager.isCorePacket(packetType)) {
                String ext = "none";
                if (packetType == 0x0008) {
                    ext = com.stars.server.connector.ConnectorUtil.getString(buf, buf.readerIndex() + 10);
                }
//                LogUtil.info("发包|account:{}|type:{}|subtype:{}|len:{}|tid:{}|serverId:{}|ext:{}",
//                        session == null ? null : session.account(),
//                        String.format("0x%04X", packetType),
//                        String.format("0x%02X", subtype),
//                        buf.readableBytes(),
//                        Thread.currentThread().getId(),
//                        serverId,
//                        ext);
            }
            if (!handleProtocol(session, packetType, buf,ctx) && session != null) {
                long packetLength = writeToFrontend(session.frontendChannel(), buf);

                // 统计
//                ConnectorStat stat = Connector.currentStat.get();
//                stat.packetNumber(packetType, 1L);
//                stat.packetLength(packetType, packetLength);
//                if (session.stat() != null) { // 针对连接的统计（因连接时抽样统计，所以需要增加判断）
//                    session.stat().packetNumber(packetType, 1L);
//                    session.stat().packetLength(packetType, packetLength);
//                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isWritable()) {
            com.stars.util.LogUtil.info("游戏服连接可写, serverId={}, tid={}", serverId, Thread.currentThread().getId());
            for (com.stars.server.connector.FrontendSession session : com.stars.server.connector.Connector.idToSession().values()) {
                session.frontendChannel().config().setAutoRead(true);
            }
        } else {
            com.stars.util.LogUtil.info("游戏服连接不可写, serverId={}, tid={}", serverId, Thread.currentThread().getId());
            for (com.stars.server.connector.FrontendSession session : com.stars.server.connector.Connector.idToSession().values()) {
                session.frontendChannel().config().setAutoRead(false);
            }
        }
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        com.stars.util.LogUtil.error("", cause);
        super.exceptionCaught(ctx, cause);
    }

    private static byte[] gzipData(byte[] payload) {
        byte[] data = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzos = new GZIPOutputStream(baos);
            gzos.write(payload);
            gzos.close();
            data = baos.toByteArray();
            baos.close();
        } catch (IOException e) {
            com.stars.util.LogUtil.error(e.getMessage(), e);
        } finally {
            return data;
        }
    }

    private static byte[] encrypt(byte[] payload) {
        for (int i = 1; i <= payload.length; i++) {
            if (i % 5 == 0) {
                payload[(i - 1)] = (byte) (payload[(i - 1)] + 7);
            } else if (i % 4 == 0) {
                payload[(i - 1)] = (byte) (payload[(i - 1)] + 3);
            } else {
                payload[(i - 1)] = (byte) (payload[(i - 1)] + i * 7 % 10);
            }
        }
        return payload;
    }

    private boolean handleProtocol(com.stars.server.connector.FrontendSession session, short packetType, ByteBuf inBuf, ChannelHandlerContext ctx) {
        /* 统计数据包 */
        // WARNING: 漏了压缩这一步骤
//        ConnectorStat stat = Connector.currentStat.get();
//        stat.packetNumber(packetType, 1L);
//        stat.packetLength(packetType, inBuf.readableBytes());
//        if (session != null && session.stat() != null) { // 针对连接的统计（因连接时抽样统计，所以需要增加判断）
//            session.stat().packetNumber(packetType, 1L);
//            session.stat().packetLength(packetType, inBuf.readableBytes());
//        }
        /* 处理协议 */
        switch (packetType) {
            case com.stars.server.connector.Connector.PROTO_PONG: case com.stars.server.connector.Connector.PROTO_CLIENT_HEARTBEAT2:
                timestamp = System.currentTimeMillis();
                return true;
            case com.stars.server.connector.Connector.PROTO_UNBIND_BACKEND:
                if (session != null && session.state() == com.stars.server.connector.FrontendSession.BINDING) {
                    com.stars.util.LogUtil.info("游戏服解除连接绑定, cid={}, account={}",
                            session.connectionId(), session.account());
                    session.account(null);
                    session.backendChannelIndex(-1);
                    session.state(com.stars.server.connector.FrontendSession.UNBINDING);
                }
                return true;
            case com.stars.server.connector.Connector.PROTO_CLOSE_FRONTEND:
                if (session != null) {
                    com.stars.util.LogUtil.info("游戏服关闭客户端连接, cid={}, account={}",
                            session.connectionId(), session.account());
                    session.frontendChannel().close();
                    session.state(com.stars.server.connector.FrontendSession.UNBINDING);
                }
                return true;
            case com.stars.server.connector.Connector.PROTO_MODIFY_ROUTE: // 绑定连接
                if (session != null) {
                	inBuf.readShort();
                    inBuf.readInt();
                    int newServerId = inBuf.readInt();
                    long roleId = inBuf.readLong();  			
        			
                    com.stars.server.connector.BackendSession backendSession = com.stars.server.connector.Connector.getBackendSession(newServerId);
                    if (backendSession == null) {
                        com.stars.util.LogUtil.info("游戏服改变连接绑定, 不存在服务ID, cid={}, account={}, osid={}, nsid={}",
                                session.connectionId(), session.account(), session.backendChannelIndex(), newServerId);
                        return true;
                    }
                    com.stars.util.LogUtil.info("游戏服改变连接绑定, cid={}, account={}, osid={}, nsid={}",
                            session.connectionId(), session.account(), session.backendChannelIndex(), newServerId);
                    com.stars.server.connector.handler.FrontendInboundHandler.bindBackend(session, newServerId, session.account());
//                    // 发往后端
                    ByteBuf outBuf = backendSession.channel().alloc().buffer();
                    outBuf.writeShort(com.stars.server.connector.Connector.PROTO_REGISTRATION);
                    outBuf.writeInt(0); // packet id(坑)
                    outBuf.writeLong(roleId);
                    FrontendInboundHandler.writeToBackend(backendSession.channel(), session.connectionId(), outBuf);
                }
                return true;
                
            case com.stars.server.connector.Connector.PROTO_PUBLIC_CONFIG://公共服配置
            	LogUtil.info("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
            	inBuf.readShort(); // packet type
                inBuf.readInt(); // packet id
				byte size = inBuf.readByte();
				for (byte i =0;i<size;i++) {
					com.stars.server.connector.Connector.backendConfig.add(inBuf.readInt(), new NewByteBuffer(inBuf).readString(), inBuf.readInt());
				}
				Map<Integer, com.stars.server.connector.runnable.connection.BackendChannelBuildTask> mapOfEVent =
						com.stars.server.connector.Connector.backendChannelBuildTasks.get(ctx.channel().eventLoop().hashCode());
				if (mapOfEVent == null) {
					mapOfEVent = new HashMap<Integer, com.stars.server.connector.runnable.connection.BackendChannelBuildTask>();
					com.stars.server.connector.Connector.backendChannelBuildTasks.put(ctx.channel().eventLoop().hashCode(), mapOfEVent);
				}
				Iterator<com.stars.server.connector.BackendAddress> it = com.stars.server.connector.Connector.backendConfig.iterator();
				while (it.hasNext()) {
					BackendAddress bAddress = it.next();
					if (!mapOfEVent.containsKey(bAddress.getServerId())) {
						com.stars.server.connector.runnable.connection.BackendChannelBuildTask buildTask = new BackendChannelBuildTask(bAddress, ctx.channel().eventLoop() , null);
						ctx.executor().submit(buildTask);
						mapOfEVent.put(bAddress.getServerId(), buildTask);
					}
				}
            	return true;
				
        }
        return false;
    }

    /**
     * 往客户端连接写入数据包
     * @param ch
     * @param inBuf
     * @return 负载长度（因为经过压缩处理）
     */
    public static long writeToFrontend(Channel ch, ByteBuf inBuf) {
        byte[] payload = new byte[inBuf.readableBytes()];
        inBuf.readBytes(payload);
        // compress data if required
        byte compressType = 2;
        if (payload.length > 4096) {
            payload = gzipData(payload);
            compressType = 1;
        }
        // encrypt
        payload = encrypt(payload);
        // write the buffer
        ByteBuf outBuf = ch.alloc().buffer();
        try {
            outBuf.writeByte((byte) -82);
            outBuf.writeInt(payload.length + 1);
            outBuf.writeByte(compressType);
            outBuf.writeBytes(payload);
            outBuf.writeByte((byte) -81);

//            StringBuilder sb = new StringBuilder();
//            sb.append("[");
//            for (int i = outBuf.readerIndex(); i < outBuf.writerIndex(); i++) {
//                sb.append("0x").append(Integer.toHexString(0xFF & outBuf.getByte(i)));
//                if (i != outBuf.writerIndex() - 1) {
//                    sb.append(" ");
//                }
//            }
//            sb.append("]");
//            LogUtil.info("连接|发包|tid:{}|cid:{}|len:{}|payload:{}", Thread.currentThread().getId(), connId, payload.length+1, sb.toString());

            // send the message
            ch.unsafe().write(outBuf, new DefaultChannelPromise(ch)); // todo: write it directly!!
            ch.unsafe().flush();
            return payload.length + 1; // 1为压缩类型
        } catch (Exception e) {
            ReferenceCountUtil.release(outBuf);
            throw e;
        }
    }
}
