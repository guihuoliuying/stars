package com.stars.server.fight;


import com.stars.bootstrap.AbstractServer;
import com.stars.bootstrap.BootstrapConfig;
import com.stars.network.server.codec.GamePacketDecoder;
import com.stars.network.server.config.ServerNetConfig;
import com.stars.network.server.handler.MainServerOutboundHandler;
import com.stars.network.server.handler.MultiServerHandler;
import com.stars.server.Business;
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
 * @author dengzhou
 *跨服，或者是无状态的战斗服
 *
 *
 */
public class MultiServer extends AbstractServer {
	
	private com.stars.server.Business business;
	
	public MultiServer(BootstrapConfig config){
		super(config.getServerName());
		setConfig(config);
		com.stars.util.LogUtil.init();
		try {
            setStartTimestamp();
            business = (com.stars.server.Business) ClassLoader.getSystemClassLoader().loadClass(config.getBusinessName()).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("找不到业务处理类" + config.getBusinessName());
        }
	}

	@Override
	public void start() throws Exception {
		business.init();
		initNet(getConfig().getServerPort());
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}
	
	private void initNet(int port){
		
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
                                            pipeline.addLast("inbound", new MultiServerHandler(business));
                                            // handle connector protocol(remove session)
                                            pipeline.addLast(new MainServerOutboundHandler());
                                        }
                                    });
                    ChannelFuture f = b.bind(sPort).sync();
                    CoreLogger.info("服务接受网关socket端口：" + sPort);
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
//                                    pipeline.addLast("inbound", new MultiServerHandler(business));
//                                    // handle connector protocol(remove session)
////                                    pipeline.addLast(new MainServerOutboundHandler());
//                                }
//                            });
//            ChannelFuture f = b.bind(port).sync();
//            CoreLogger.info("服务接受网关socket端口：" + port);
//        } catch (Exception e) {
//            CoreLogger.error(e.getMessage(), e);
//        }
    
	}

	public Business getBusiness() {
		return business;
	}
}
