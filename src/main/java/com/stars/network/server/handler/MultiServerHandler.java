package com.stars.network.server.handler;


import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.session.GameSession;
import com.stars.server.Business;
import com.stars.server.connector.Connector;
import com.stars.util.log.CoreLogger;
import com.stars.core.rpc.RpcManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dengzhou
 *         跨服的网络处理handler
 */
public class MultiServerHandler extends SimpleChannelInboundHandler<Object> {

    private com.stars.server.Business dispatcher;

    public MultiServerHandler(Business dispatcher) {
        this.dispatcher = dispatcher;
    }


    private Map<Integer, com.stars.network.server.session.GameSession> sessionMap = new HashMap<>();

    public Map<Integer, com.stars.network.server.session.GameSession> sessionMap() {
        return sessionMap;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//	        CoreLogger.error("后端连接断开");
//	        Iterator<GameSession> itor = sessionMap.values().iterator();
//	        while (itor.hasNext()) {
//	            GameSession session = itor.next();
//	            itor.remove();
//	            // 通知业务层玩家掉线
//	            long roleId = session.getRoleId();
//	            Packet p = new Disconnected(roleId);
//	            p.setSession(session);
//	            MainServer.getBusiness().dispatch(p);
//	            if (roleId != 0) {
//	                SessionManager.remove(roleId, session);
//	                CoreLogger.error("channelInactive, roleId = " + roleId);
//	            } else {
//	                CoreLogger.error("channelInactive, roleId = 0");
//	            }
//	        }
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        final int connectionId = buf.readInt();
        if (connectionId == -1) {
            handleHeartbeat(ctx);
            return;
        }
        if (!sessionMap.containsKey(connectionId)) {
            com.stars.network.server.session.GameSession session = new com.stars.network.server.session.GameSession();
            session.setConnectionId(connectionId);
            session.setChannel(ctx.channel());
            sessionMap.put(connectionId, session);
        }
        GameSession session = sessionMap.get(connectionId);
        if (session != null) {
            final Packet packet = Packet.newPacket(new NewByteBuffer(buf), session);
            if (packet == null) {
                CoreLogger.error("解析packet为null");
                return;
            }
            if (RpcManager.handlePacket(packet)) { // 处理Rpc请求
                return;
            }
            dispatcher.dispatch(packet);
        }
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
