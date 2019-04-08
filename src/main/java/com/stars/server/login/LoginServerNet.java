package com.stars.server.login;

import com.stars.server.connector.handler.PacketDecoder;
import com.stars.util.LogUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Created by liuyuheng on 2015/12/28.
 */
public class LoginServerNet {
    public static void bind(EventLoopGroup bossGroup, EventLoopGroup workerGroup, String ip, int port) throws Exception {
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_SNDBUF, 128 * 1024)
                    .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                    .handler(new LoggingHandler(LogLevel.ERROR))
                    .childHandler(
                            new ChannelInitializer<NioSocketChannel>() {
                                @Override
                                protected void initChannel(NioSocketChannel ch) throws Exception {
                                    ch.pipeline().addLast(new PacketDecoder(Integer.MAX_VALUE, 1, 4, 1, 0));
                                    ch.pipeline().addLast(new LoginServerHandler());
                                }
                            });
            b.bind(ip, port).sync();
            com.stars.util.LogUtil.info("登录服绑定socket端口成功,ip:" + ip + ",port:" + port);
        } catch (Exception e) {
        	LogUtil.error("登录服绑定socket端口失败", e);
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            throw e;
        }
    }
}
