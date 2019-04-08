package com.stars.server.proxy.weaknetwork;

import com.stars.util.log.CoreLogger;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.TimeUnit;

/**
 * Created by zws on 2015/10/13.
 */
public class NetProxyHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private Channel upstreamChannel;

    class DelayTask implements Runnable{

        private ByteBuf buf;

        public DelayTask(ByteBuf buf){
            this.buf = buf;
        }

        @Override
        public void run() {
            upstreamChannel.writeAndFlush(this.buf);
        }
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        if (com.stars.server.proxy.weaknetwork.NetProxy.flag) {
            try {
                NetProxySession session = com.stars.server.proxy.weaknetwork.NetProxy.getSession(ctx.channel());
                if(session == null){
                    com.stars.util.log.CoreLogger.info("代理session为空，此消息不转发");
                    return;
                }
                /* 统计相关数据 */
                session.increaseReadCount(buf.readableBytes());
                if (upstreamChannel != null) { // 用作代理时才进行转发
                    int delay = session.getDelay();
                    if (delay > 0) {
                        com.stars.server.proxy.weaknetwork.NetProxy.service.schedule(new DelayTask(buf.retain()), delay,
                                TimeUnit.MILLISECONDS);
                        return;
                    }
                    buf.retain();
                    upstreamChannel.writeAndFlush(buf);
                }
            } catch (Exception e) {
                if(buf.refCnt() > 1){
                    com.stars.util.log.CoreLogger.error("代理回收buf！！！引发异常：",e);
                    ReferenceCountUtil.release(buf);

                }
            }
        } else {
            // drop it
        }
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx0) throws Exception {

        NetProxySession session = new NetProxySession(com.stars.server.proxy.weaknetwork.NetProxy.nextId(), ctx0.channel());
        com.stars.server.proxy.weaknetwork.NetProxy.addSession(session);
        // 如果是虚假者，就不建立连接
        if (com.stars.server.proxy.weaknetwork.NetProxy.config.isMock()) {
            ctx0.channel().config().setAutoRead(true);
            super.channelActive(ctx0);
            return;
        }
        // build a connection to upstream
        Bootstrap bootstrap = new Bootstrap()
                .group(com.stars.server.proxy.weaknetwork.NetProxy.group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_SNDBUF, 64 * 1024)
                .option(ChannelOption.SO_RCVBUF, 64 * 1024)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg)
                                    throws Exception {
                                try {
                                    ctx0.channel().writeAndFlush(msg.retain());
                                } catch (Exception e) {
                                    ReferenceCountUtil.release(msg);
                                }
                            }
                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                                    throws Exception {
                                ctx0.close();
                                super.exceptionCaught(ctx, cause);
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx)
                                    throws Exception {
                                ctx0.close();
                                super.channelInactive(ctx);
                            }
                        });
                    }
                });
        bootstrap.connect(com.stars.server.proxy.weaknetwork.NetProxy.config.upstreamIp(), com.stars.server.proxy.weaknetwork.NetProxy.config.upstreamPort()).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    upstreamChannel = future.channel();
                    ctx0.channel().config().setAutoRead(true);
                } else {
                    com.stars.util.log.CoreLogger.info("upstream server连接失败");
                    ctx0.close();
                }
            }
        });
        super.channelActive(ctx0);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        com.stars.util.log.CoreLogger.error("", cause);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NetProxy.removeSession(ctx.channel());
        if (upstreamChannel != null) {
            upstreamChannel.close();
        }
        CoreLogger.info("channel inactive");
        super.channelInactive(ctx);
    }
}
