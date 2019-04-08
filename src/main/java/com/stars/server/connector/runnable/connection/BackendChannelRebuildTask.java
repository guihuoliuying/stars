package com.stars.server.connector.runnable.connection;

import com.stars.server.connector.BackendAddress;
import com.stars.server.connector.BackendSession;
import com.stars.server.connector.Connector;
import com.stars.server.connector.handler.BackendInboundHandler;
import com.stars.server.connector.handler.PacketDecoder;
import com.stars.util.LogUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

import java.util.concurrent.TimeUnit;

/**
 * Created by zws on 2015/8/24.
 */
public class BackendChannelRebuildTask implements Runnable {

    private int serverId;
    private com.stars.server.connector.BackendAddress address;
    private EventLoop eventLoop;

    public BackendChannelRebuildTask(
            int serverId, BackendAddress address, EventLoop eventLoop) {
        this.serverId = serverId;
        this.address = address;
        this.eventLoop = eventLoop;
    }

    @Override
    public void run() {
        if (Connector.isRunning) {
            final Bootstrap bootstrap = new Bootstrap()
                    .group(eventLoop)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
//                    .option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 32 * 1024)
                    .option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, Connector.config.lwm())
//                    .option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 128 * 1024)
                    .option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, Connector.config.hwm())
                    .option(ChannelOption.SO_SNDBUF, 128 * 1024)
                    .option(ChannelOption.SO_RCVBUF, 128 * 1024)
                    .attr(AttributeKey.valueOf("isBackend"), true)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new PacketDecoder(Integer.MAX_VALUE, 1, 4, 1, 0));
                            ch.pipeline().addLast(new BackendInboundHandler(serverId, address));
                        }
                    });
            bootstrap.connect(address.ip, address.port).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                    	com.stars.util.LogUtil.info("重连游戏服成功, serverId={}, threadId={}",
                                serverId, Thread.currentThread().getId());
                        com.stars.server.connector.BackendSession ch = new BackendSession(serverId, future.channel());
                        Connector.setBackendSession(serverId, ch);
                    } else {
                    	LogUtil.info("重连游戏服失败, serverId={}, threadId={}",
                                serverId, Thread.currentThread().getId());
                        eventLoop.schedule(BackendChannelRebuildTask.this, 1, TimeUnit.SECONDS);
                    }
                }
            });
        }
    }

}
