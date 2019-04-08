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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by zws on 2015/8/21.
 */
public class BackendChannelBuildTask implements Runnable {

//    private int serverId;
    private com.stars.server.connector.BackendAddress address;
    private EventLoop eventLoop;
    private com.stars.server.connector.Connector connector;
    private CountDownLatch latch;

    public BackendChannelBuildTask(com.stars.server.connector.BackendAddress address, EventLoop eventLoop, CountDownLatch latch) {
        this.address = address;
        this.eventLoop = eventLoop;
        this.latch = latch;
    }

    public BackendChannelBuildTask(BackendAddress address, EventLoop eventLoop) {
        this.address = address;
        this.eventLoop = eventLoop;
    }

    @Override
    public void run() {
        if (com.stars.server.connector.Connector.isRunning) {
            final Bootstrap bootstrap = new Bootstrap()
                    .group(eventLoop)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_REUSEADDR, true)
//                    .option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 32 * 1024)
                    .option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, com.stars.server.connector.Connector.config.lwm())
//                    .option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 128 * 1024)
                    .option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, com.stars.server.connector.Connector.config.hwm())
                    .option(ChannelOption.SO_SNDBUF, 128 * 1024)
                    .option(ChannelOption.SO_RCVBUF, 128 * 1024)
                    .attr(AttributeKey.valueOf("isBackend"), true)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new PacketDecoder(Integer.MAX_VALUE, 1, 4, 1, 0));
                            ch.pipeline().addLast(new BackendInboundHandler(address.serverId, address));
                        }
                    });
            bootstrap.connect(address.ip, address.port).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        com.stars.server.connector.BackendSession ch = new BackendSession(address.serverId, future.channel());
                        Connector.setBackendSession(address.serverId, ch);
                        if (latch != null) {
                            latch.countDown();
                        }
                        com.stars.util.LogUtil.info("连接游戏服{}({}:{})成功", address.serverId, address.ip, address.port);
                    } else {
                    	LogUtil.info("连接游戏服" + address.serverId + "失败...重新连接");
                        eventLoop.schedule(BackendChannelBuildTask.this, 5, TimeUnit.SECONDS);
                    }
                }
            });
        }
    }
}
