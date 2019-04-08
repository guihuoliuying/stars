package com.stars.server.login2.netty;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.server.login2.LoginServer2;
import com.stars.server.login2.helper.LoginNetwork;
import com.stars.server.login2.task.LVerifyTask;
import com.stars.util.ExecuteManager;
import com.stars.util.LogUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.concurrent.RejectedExecutionException;

/**
 * Created by zhaowenshuo on 2016/2/1.
 */
public class LoginHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private Channel nettyChannel;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        this.nettyChannel = ctx.channel();
        short packetType = buf.readShort();
        handleProtocol(packetType, buf, ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace(); // fixme:
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            switch (((IdleStateEvent) evt).state()) {
                case READER_IDLE:
                case WRITER_IDLE:
                    ctx.close();
                    break;
            }
        }
    }

    private void handleProtocol(short packetType, ByteBuf buf, Channel nettyChannel) {
        switch (packetType) {
            case com.stars.server.login2.LoginServer2.PROTO_SERVER_HEARTBEAT:
                break;
            case LoginServer2.PROTO_SERVER_LOGIN_CHECK:
                NewByteBuffer newBuf = new NewByteBuffer(buf);
                int channelId = newBuf.readInt();
                String extent = newBuf.readString();
                com.stars.util.LogUtil.info("{} verify: {} - {}", nettyChannel, channelId, extent);
                try {
                    ExecuteManager.execWithThrowable(new LVerifyTask(channelId, extent, nettyChannel));
                } catch (RejectedExecutionException e) {
                    LoginNetwork.fail(nettyChannel, "服务繁忙"); // 实际上是catch不到异常
                    com.stars.util.LogUtil.error("业务线程池繁忙, cause={}: {}",
                            e.getClass().getSimpleName(), e.getMessage());
                }
                break;
            default:
            	LogUtil.error("Illegal Packet Type: {}", packetType);
                break;
        }
    }

}
