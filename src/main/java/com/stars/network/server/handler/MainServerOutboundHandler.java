package com.stars.network.server.handler;

import com.stars.server.connector.packet.CloseFrontendM2nPacket;
import com.stars.server.connector.packet.UnbindBackendM2nPacket;
import com.stars.server.main.MainServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * Created by zws on 2015/8/27.
 */
public class MainServerOutboundHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
            throws Exception {

        if (msg instanceof com.stars.server.connector.packet.CloseFrontendM2nPacket) {
            // 负载计算
            int payload = com.stars.server.main.MainServer.payload.decrementAndGet();
//            CoreLogger.info("游戏服负载，{}", payload);
            // 移除connectionId
            com.stars.network.server.handler.MainServerHandler2 handler = (com.stars.network.server.handler.MainServerHandler2) ctx.pipeline().get("inbound");
            handler.sessionMap().remove(((CloseFrontendM2nPacket) msg).getSession().getConnectionId());
        }

        if (msg instanceof com.stars.server.connector.packet.UnbindBackendM2nPacket) {
            // 负载计算
            MainServer.payload.decrementAndGet();
//            CoreLogger.info("游戏服负载，{}", MainServer.payload.get());
            // 移除connectionId
            com.stars.network.server.handler.MainServerHandler2 handler = (MainServerHandler2) ctx.pipeline().get("inbound");
            handler.sessionMap().remove(((UnbindBackendM2nPacket) msg).getSession().getConnectionId());
        }

        super.write(ctx, msg, promise);
    }
}
