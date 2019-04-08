package com.stars.network.server;

import com.stars.network.server.codec.GamePacketDecoder;
import com.stars.network.server.codec.GmPacketDecoder;
import com.stars.network.server.config.ServerNetConfig;
import com.stars.network.server.handler.MainServerGmHandler;
import com.stars.network.server.handler.MainServerHandler2;
import com.stars.network.server.handler.MainServerOutboundHandler;
import com.stars.util.LogUtil;
import com.stars.util.log.CoreLogger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

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
                                            //encode
//                                            pipeline.addLast(new GamePacketEncoder());
                                            //decode
                                            pipeline.addLast(new GamePacketDecoder(Integer.MAX_VALUE, 1, 4, 1, 0));
                                            //idle
                                            pipeline.addLast(new IdleStateHandler(6, 0, 0));
                                            //execute
                                            pipeline.addLast("inbound", new MainServerHandler2());
                                            // handle connector protocol(remove session)
                                            pipeline.addLast(new MainServerOutboundHandler());
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
//        try {
//            // Configure the server.
//            EventLoopGroup bossGroup = new NioEventLoopGroup(ServerNetConfig.launcherToServer_Boss_ThreadCount);
//            EventLoopGroup workerGroup = new NioEventLoopGroup(ServerNetConfig.launcherToServer_Worker_ThreadCount);
//            ServerBootstrap b = new ServerBootstrap();
//            b.group(bossGroup, workerGroup)
//                    .channel(NioServerSocketChannel.class)
//                    .option(ChannelOption.SO_BACKLOG, 100)
//                    .option(ChannelOption.SO_REUSEADDR, true)
//                    .handler(new LoggingHandler(LogLevel.ERROR))
//                    .childHandler(
//                            new ChannelInitializer<NioSocketChannel>() {
//                                @Override
//                                protected void initChannel(NioSocketChannel ch) throws Exception {
//                                    ChannelPipeline pipeline = ch.pipeline();
//                                    //encode
////                                    pipeline.addLast(new GamePacketEncoder());
//                                    //decode
//                                    pipeline.addLast(new GamePacketDecoder(Integer.MAX_VALUE, 1, 4, 1, 0));
//                                    //idle
//                                    pipeline.addLast(new IdleStateHandler(6, 0, 0));
//                                    //execute
//                                    pipeline.addLast("inbound", new MainServerHandler2());
//                                    // handle connector protocol(remove session)
//                                    pipeline.addLast(new MainServerOutboundHandler());
//                                }
//                            });
//            ChannelFuture f = b.bind(port).sync();
//            CoreLogger.info("服务接受网关socket端口：" + port);
//            f.channel().closeFuture().sync();
//            LogUtil.info("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
//        } catch (Exception e) {
//            CoreLogger.error(e.getMessage(), e);
//        }
    }

    public static void startupGmSockt(int port) {
        final int sPort = port;
        new Thread(new Runnable() {
            @Override
            public void run() {
                EventLoopGroup bossGroup = new NioEventLoopGroup(1);
                EventLoopGroup workerGroup = new NioEventLoopGroup(1);
                try {
                    ServerBootstrap b = new ServerBootstrap();
                    b.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(
                                    new ChannelInitializer<NioSocketChannel>() {
                                        @Override
                                        protected void initChannel(NioSocketChannel ch) throws Exception {
                                            ChannelPipeline pipeline = ch.pipeline();
                                            // decode
                                            pipeline.addLast(new GmPacketDecoder(Short.MAX_VALUE, 0, 4));
                                            //idle
                                            pipeline.addLast(new IdleStateHandler(6, 0, 0));
                                            // packet handler
                                            pipeline.addLast(new MainServerGmHandler());
                                            // execute
                                        }
                                    });
                    ChannelFuture f = b.bind(sPort).sync();
                    CoreLogger.info("服务接受gm socket端口：" + sPort);
                    f.channel().closeFuture().sync();
                } catch (Throwable e) {
                    System.out.println("gm socket启动失败");
                    e.printStackTrace();
                    System.exit(-1);
                } finally {
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            }
        }).start();
    }

}
