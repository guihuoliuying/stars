package com.stars.server.proxy.weaknetwork;

import com.stars.bootstrap.AbstractServer;
import com.stars.util.backdoor.BackdoorServer;
import com.stars.util.backdoor.command.CommandFactory;
import com.stars.util.log.CoreLogger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zws on 2015/10/13.
 */
public class NetProxy extends AbstractServer {

    public static NetProxyConfig config;
    public static volatile boolean flag = true;
    public static final NioEventLoopGroup group = new NioEventLoopGroup(4);
    public static volatile Channel serverChannel;

    private static AtomicInteger idCreator = new AtomicInteger(0);

    public static int nextId(){
        return idCreator.incrementAndGet();
    }

    //存放游戏服与代理之间的session，在游戏服连过来的时候就会add
    private static ConcurrentHashMap<Integer, NetProxySession> proxySessions = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<Integer, NetProxySession> getSessionMap() {
        return proxySessions;
    }

    public static void addSession(NetProxySession session) {
        proxySessions.put(session.getSessionId(), session);
    }

    public static NetProxySession getSession(int sessionId) {
        return proxySessions.get(sessionId);
    }

    public static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    public static NetProxySession getSession(Channel channel) {
        Iterator iter = proxySessions.entrySet().iterator();
        Map.Entry<Integer, NetProxySession> entry;
        while (iter.hasNext()) {
            entry = (Map.Entry<Integer, NetProxySession>) iter.next();
            if (entry.getValue().getChannel().hashCode() == channel.hashCode()) {
                return entry.getValue();
            }
        }
        return null;
    }



    public static void removeSession(Channel channel){
        NetProxySession session = getSession(channel);
        if(session != null){
            proxySessions.remove(session.getSessionId());
        }
    }

    public NetProxy() {
        super("net");
    }

    @Override
    public void start() throws Exception {
        config = NetProxyConfig.load();
        if (config.upstreamIp() == null) {
            System.out.println("type: mock");
        } else {
            System.out.println("type: proxy");
        }
        initCommand(); // 初始化命令
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(new NioEventLoopGroup(20), group)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.AUTO_READ, false)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_SNDBUF, 64 * 1024)
                .childOption(ChannelOption.SO_RCVBUF, 64 * 1024)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new NetProxyHandler());
                    }
                });
        bootstrap.bind(config.proxyIp(), config.proxyPort()).addListener(
                new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            serverChannel = future.channel();
                            com.stars.util.log.CoreLogger.info("Ok");
                        } else {
                            CoreLogger.info("Not Ok");
                        }
                    }
                });

        // 启动后门
        Thread consoleListener = new Thread(new BackdoorServer("localhost", 52014));
        consoleListener.start();
    }

    @Override
    public void stop() {

    }

    private void initCommand() {
        com.stars.util.backdoor.command.CommandFactory.register(new NetpCommand());
        CommandFactory.register(new SessionCommand());
    }
}
