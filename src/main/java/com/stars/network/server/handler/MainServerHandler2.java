package com.stars.network.server.handler;

import com.stars.bootstrap.ServerManager;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.config.ServerConfig;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.network.server.session.GameSession;
import com.stars.network.server.session.SessionManager;
import com.stars.server.connector.Connector;
import com.stars.server.connector.packet.FrontendClosedN2mPacket;
import com.stars.server.connector.packet.SendPubServerConfig;
import com.stars.server.main.MainServer;
import com.stars.server.main.message.Disconnected;
import com.stars.util.ExecuteManager;
import com.stars.util.log.CoreLogger;
import com.stars.core.rpc2.RpcManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zws on 2015/8/25.
 */
public class MainServerHandler2 extends SimpleChannelInboundHandler<Object> {

    private Map<Integer, com.stars.network.server.session.GameSession> sessionMap = new HashMap<>();

    public Map<Integer, com.stars.network.server.session.GameSession> sessionMap() {
        return sessionMap;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	super.channelInactive(ctx);
        com.stars.util.log.CoreLogger.error("后端连接断开");
        Iterator<com.stars.network.server.session.GameSession> itor = sessionMap.values().iterator();
        while (itor.hasNext()) {
            com.stars.network.server.session.GameSession session = itor.next();
            itor.remove();
            // 负载处理
            // fixme: there may be a bug, when channel inactive and role logout, the count may be decrease twice
            com.stars.server.main.MainServer.payload.decrementAndGet();
            // 通知业务层玩家掉线
            long roleId = session.getRoleId();
            Packet p = new com.stars.server.main.message.Disconnected(roleId);
            p.setSession(session);
            com.stars.server.main.MainServer.getBusiness().dispatch(p);
            if (roleId != 0) {
                com.stars.network.server.session.SessionManager.remove(roleId, session);
                com.stars.util.log.CoreLogger.error("channelInactive, roleId = " + roleId);
            } else {
                com.stars.util.log.CoreLogger.error("channelInactive, roleId = 0");
            }
        }
//        CoreLogger.info("游戏服负载，{}", MainServer.payload.get());
        com.stars.network.server.session.SessionManager.getChannelSet().remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        final int connectionId = buf.readInt();
        // handle heartbeat
        if (connectionId == -1) {
            handleHeartbeat(ctx);
            return;
        }
        // new connection
        if (!sessionMap.containsKey(connectionId)) {
            com.stars.network.server.session.GameSession session = new com.stars.network.server.session.GameSession();
            session.setConnectionId(connectionId);
            session.setChannel(ctx.channel());

            // 过载判断
            if (com.stars.server.main.MainServer.payload.incrementAndGet() > ServerConfig.thresholdOfPayload) {
                com.stars.util.log.CoreLogger.info("游戏服过载, load={}", com.stars.server.main.MainServer.payload.get());
                PacketManager.closeFrontend(session);
                return;
            }

            sessionMap.put(connectionId, session);
        }
        com.stars.network.server.session.GameSession session = sessionMap.get(connectionId);
        if (session != null) {
            final Packet packet = Packet.newPacket(new NewByteBuffer(buf), session);
            if (packet == null) {
                com.stars.util.log.CoreLogger.error("解析packet为null");
                return;
            }

            if (PacketManager.isCorePacket(packet)) {
                if (RpcManager.handlePacket(packet)) {
                    // no-op
                } else if (packet instanceof FrontendClosedN2mPacket) {
                    // 前端连接断开
                    int payload = com.stars.server.main.MainServer.payload.decrementAndGet();
//                    CoreLogger.info("游戏服负载，{}", MainServer.payload.get());
                    sessionMap.remove(connectionId);
                    long roleId = session.getRoleId();
                    if (roleId != 0) {
                        com.stars.network.server.session.SessionManager.remove(roleId, session); // fixme: 1 problem. memory leak.
                    }
                    Packet p = new Disconnected(roleId);
                    p.setSession(session);
                    com.stars.server.main.MainServer.getBusiness().dispatch(p);
                    if (roleId != 0) {
                        com.stars.util.log.CoreLogger.error("channelInactive, roleId={}, load={}", roleId, payload);
                    } else {
                        CoreLogger.error("channelInactive, roleId=0, load={}", payload);
                    }

                } else {
                    ExecuteManager.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                packet.execPacket();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } else {
                MainServer.getBusiness().dispatch(packet);
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	
    	super.channelActive(ctx);
    	SessionManager.getChannelSet().add(ctx.channel());
    	ConcurrentHashMap<String, Properties> pubProps = ServerManager.getServer().getConfig().getPubProps();
    	com.stars.server.connector.packet.SendPubServerConfig serverConfig = new SendPubServerConfig();
    	Collection<Properties>col= pubProps.values();
		for (Properties properties : col) {
			serverConfig.addBackendAddress(Integer.parseInt(properties.getProperty("serverId")),
					properties.getProperty("serverIp"),
					Integer.parseInt(properties.getProperty("serverPort")));
		}
		com.stars.network.server.session.GameSession gSession = new GameSession();
		gSession.setChannel(ctx.channel());
		PacketManager.send(gSession, serverConfig);
    }
    
    private void handleHeartbeat(ChannelHandlerContext ctx) {
        ByteBuf outBuf = ctx.alloc().buffer();
        outBuf.writeByte(-82);
        outBuf.writeInt(6);
        outBuf.writeInt(-1);
        outBuf.writeShort(Connector.PROTO_PONG);
        outBuf.writeByte(-81);
        ctx.channel().unsafe().write(outBuf, new DefaultChannelPromise(ctx.channel()));
        ctx.channel().unsafe().flush();
    }

}
