package com.stars.startup.payserver;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.bootstrap.ServerManager;
import com.stars.core.schedule.SchedulerManager;
import com.stars.multiserver.payServer.PayServerServieManager;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.server.Business;
import com.stars.server.main.actor.ActorServer;
import com.stars.services.ServiceHelper;
import com.stars.startup.MainStartup;
import com.stars.util.LogUtil;
import com.stars.core.actor.ActorSystem;
import com.stars.util.log.CoreLogger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class PayServerStartup implements Business {

	@Override
	public void init() throws Exception {
//		if(!HotUpdateManager.init(LogManager.getLogger("console"), LogManager.getLogger("console"))){
//            System.exit(0);
//        }
		MainStartup.initHotswapEnv();
		SchedulerHelper.init("./config/jobs/quartz.properties");
        SchedulerManager.init(SchedulerManager.scheduledCorePoolSize);
		ActorServer.setActorSystem(new ActorSystem()); // 初始化ActorSystem
		PacketManager.loadCorePacket();
		try {
			ServiceHelper.init(new PayServerServieManager());
			int port = ServerManager.getServer().getConfig().getHttpPort();
			startHttpNet(port);
		} catch (Throwable cause) {
			LogUtil.error(cause.getMessage(), cause);
			System.exit(-1);
		}
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispatch(Packet packet) {
		// TODO Auto-generated method stub

	}
	
	public void startHttpNet(int port){
		final int p = port;
		new Thread(new Runnable() {	
			@Override
			public void run() {
				EventLoopGroup bossGroup = new NioEventLoopGroup();
				EventLoopGroup workerGroup = new NioEventLoopGroup();
				try {
					ServerBootstrap b = new ServerBootstrap();
					b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new HttpResponseEncoder());
							ch.pipeline().addLast(new HttpRequestDecoder());
							ch.pipeline().addLast(new PayServerInboundHandler());
						}
					}).option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, true);
					ChannelFuture f = b.bind(p).sync();
					com.stars.util.log.CoreLogger.info("服务接受http端口：" + p);
					f.channel().closeFuture().sync();
				} catch (Exception e) {
					CoreLogger.error(e.getMessage(), e);
				} finally{
					workerGroup.shutdownGracefully();
					bossGroup.shutdownGracefully();
				}
			
			}
		}).start();
	}
}
