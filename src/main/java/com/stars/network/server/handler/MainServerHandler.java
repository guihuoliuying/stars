package com.stars.network.server.handler;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.network.server.session.GameSession;
import com.stars.network.server.session.SessionManager;
import com.stars.server.main.MainServer;
import com.stars.server.main.message.Disconnected;
import com.stars.util.ExecuteManager;
import com.stars.util.log.CoreLogger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Created by zd on 2015/3/11.
 */
public class MainServerHandler extends SimpleChannelInboundHandler<Object> {

    private com.stars.network.server.session.GameSession session = null;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        session.addReceivePacketCount();
        ByteBuf buf = (ByteBuf) msg;
        NewByteBuffer newByteBuffer = new NewByteBuffer(buf);
        final Packet packet = Packet.newPacket(newByteBuffer, session);
        if (packet == null) {
            com.stars.util.log.CoreLogger.error("解析packet为null");
            return;
        }
        if (PacketManager.isCorePacket(packet)) {
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
        } else {
            com.stars.server.main.MainServer.getBusiness().dispatch(packet);
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        session = new GameSession();
        session.setChannel(ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        com.stars.util.log.CoreLogger.error(cause.getMessage(), cause);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        com.stars.util.log.CoreLogger.error("channel unregistered. session = " + this.session.getConnectionId());
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (!session.isServerSession()) {
            long roleId = session.getRoleId();
            if (roleId != 0) {
                SessionManager.remove(roleId, session);
//                MainServer.getBusiness().dispatch(new Disconnected(session, roleId));
//                CoreLogger.error("channelInactive！！！session =" + roleId);
            }
            // 临时解决方案
            Packet packet = new Disconnected(roleId);
            packet.setSession(session);
            MainServer.getBusiness().dispatch(packet);
            if (roleId != 0) {
                com.stars.util.log.CoreLogger.error("channelInactive, roleId = " + roleId);
            } else {
                com.stars.util.log.CoreLogger.error("channelInactive, roleId = 0");
            }
        }
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (session.isServerSession()) {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state() == IdleState.READER_IDLE) {
                    CoreLogger.error("idle roleId = " + session.getRoleId());
                    ctx.close();
                }
            }
        }
    }

}
