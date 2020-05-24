package com.stars.network.server;

import com.stars.network.server.config.ServerNetConfig;
import com.stars.util.LogUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Created by zd on 2015/3/11.
 */
public class MainNet {

    public static void startup(int port) {
    	final int sPort = port;
    	new Thread(new Runnable() {
    		@Override
            public void run() {
                EventLoopGroup bossGroup = new NioEventLoopGroup(ServerNetConfig.launcherToServer_Boss_ThreadCount);
                EventLoopGroup workerGroup = new NioEventLoopGroup(ServerNetConfig.launcherToServer_Worker_ThreadCount);
                try {
                	ServerBootstrap b = new ServerBootstrap();
                    b.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .option(ChannelOption.SO_BACKLOG, 100)
                            .option(ChannelOption.SO_REUSEADDR, true)
                            .option(ChannelOption.TCP_NODELAY, true)
                            .handler(new LoggingHandler(LogLevel.ERROR))
                            .childHandler(
                                    new ChannelInitializer<NioSocketChannel>() {
                                        @Override
                                        protected void initChannel(NioSocketChannel ch) throws Exception {
                                            ChannelPipeline pipeline = ch.pipeline();
                                            pipeline.addLast(new HttpServerCodec());
                                            pipeline.addLast(new HttpObjectAggregator(65535));

                                        }
                                    });
                    ChannelFuture f = b.bind(sPort).sync();
                    com.stars.util.log.CoreLogger.info("服务接受网关socket端口：" + sPort);
                    f.channel().closeFuture().sync();
                } catch (Throwable e) {
                    System.out.println("网络启动失败");
                    e.printStackTrace();
                    System.exit(-1);
                } finally {
                	LogUtil.info("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            
    		}
    		
    	}).start();
    }

}
