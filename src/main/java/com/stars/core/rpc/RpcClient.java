package com.stars.core.rpc;

import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.ServerManager;
import com.stars.network.server.codec.GamePacketDecoder;
import com.stars.network.server.handler.MainServerOutboundHandler;
import com.stars.network.server.handler.MultiServerHandler;
import com.stars.network.server.packet.PacketManager;
import com.stars.network.server.session.GameSession;
import com.stars.server.Business;
import com.stars.util.LogUtil;
import com.stars.core.rpc.packet.RpcRegistrationReq;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * 连接客户端
 * Created by zhaowenshuo on 2016/11/2.
 */
public class RpcClient {

    private static EventLoopGroup group = new NioEventLoopGroup();

    static {
        // fixme: 临时方案
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                group.shutdownGracefully();
            }
        });
    }

    private String serverName;
    private int serverId;
    private String ip;
    private int port;

    private Business business;
    
    public Runnable netExceptionTask = null;

    public RpcClient(String serverName, RpcClientConnectedCallback callback) {
        try {
            BootstrapConfig config = ServerManager.getServer().getConfig();
            Properties props = config.getProps().get(serverName);
            if (serverName == null || serverName.trim().equals("")) {
                throw new IllegalArgumentException("No such server name: " + serverName);
            }
            this.serverName = serverName; // 服务名字
            this.serverId = Integer.parseInt(props.getProperty("serverId")); // 服务器id
            this.ip = props.getProperty("serverIp"); // 服务器ip
            this.port = Integer.parseInt(props.getProperty("serverPort")); // 服务器端口
            if (callback != null) {
                com.stars.core.rpc.RpcManager.callbackMap.put(serverId, callback);
            }
        } catch (Exception e) {
            com.stars.util.LogUtil.error("解析[{}]配置错误", serverName);
            throw e;
        }
    }
    
    public RpcClient(int serverId,String ip,int port, RpcClientConnectedCallback callback){
    	this.serverId = serverId;
    	this.ip = ip.trim();
    	this.serverName = this.ip;
    	this.port = port;
    	if (callback != null) {
            com.stars.core.rpc.RpcManager.callbackMap.put(serverId, callback);
        }
    }
    
    public RpcClient(String serverName, RpcClientConnectedCallback callback,Runnable netExceptionTask) {
    	this(serverName, callback);
    	this.netExceptionTask = netExceptionTask;
    }

    public RpcClient connect() throws Exception {
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        //encode
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

        bootstrap.connect(ip, port).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    com.stars.util.LogUtil.info("成功连接RPC服务[{}], serverId={}", serverName, serverId);
                    // 连接成功，发送Rpc客户端连接注册包
                    GameSession session = new GameSession();
                    session.setServerId(serverId);
                    session.setChannel(future.channel());
                    com.stars.core.rpc.RpcManager.sessionMap.put(serverId, session);
                    // rpc注册包
                    List<Integer> serverIdList = new ArrayList<Integer>();
                    serverIdList.add(ServerManager.getServer().getConfig().getServerId()); // todo: maybe more（合区
                    RpcRegistrationReq packet = new RpcRegistrationReq();
                    packet.setServerIdList(serverIdList);
                    PacketManager.send(session, packet);
                    // 回调处理
                    RpcClientConnectedCallback callback = RpcManager.callbackMap.get(serverId);
                    if (callback != null) {
                        try {
                            callback.ontCalled(serverId);
                        } catch (Throwable t) {
                            com.stars.util.LogUtil.error("", t);
                        }
                    }
                    // 添加连接关闭的监听器，如果连接断了，就重连
                    future.channel().closeFuture().addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            com.stars.util.LogUtil.info("RPC连接关闭, serverName={}, serverId={}", serverName, serverId);
                            group.schedule(netExceptionTask == null?new DefaultNetExcetionTask():netExceptionTask, 5, TimeUnit.SECONDS);

                        }
                    });
                } else {
                    // 连接不成功，重连
                    com.stars.util.LogUtil.info("重新连接RPC服务[{}], serverId={}", serverName, serverId);
                    group.schedule(netExceptionTask == null?new DefaultNetExcetionTask():netExceptionTask, 5, TimeUnit.SECONDS);
                }
            }

        });
        return this;
    }
    class DefaultNetExcetionTask implements Runnable{
    	
    	public DefaultNetExcetionTask(){}
    	@Override
    	public void run() {
    		try {
                connect();
            } catch (Throwable cause) {
                LogUtil.error("", cause);
            }
    	}
    }
}


