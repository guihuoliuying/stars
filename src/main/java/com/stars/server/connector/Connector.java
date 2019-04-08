package com.stars.server.connector;

import com.stars.bootstrap.AbstractServer;
import com.stars.bootstrap.BootstrapConfig;
import com.yinhan.hotupdate.HotUpdateManager;
import com.stars.network.server.packet.PacketManager;
import com.stars.server.connector.backdoor.ConnectorBackdoor;
import com.stars.server.connector.handler.FrontendInboundHandler;
import com.stars.server.connector.handler.PacketDecoder;
import com.stars.server.connector.runnable.connection.BackendChannelBuildTask;
import com.stars.server.connector.runnable.stat.StatSubmitTask;
import com.stars.server.connector.stat.ConnectorStat;
import com.stars.util.LogUtil;
import com.stars.util.backdoor.BackdoorServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutor;
import org.apache.log4j.LogManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zws on 2015/8/21.
 */
public class Connector extends AbstractServer {

    /* 协议号 */
    public static final short PROTO_CLIENT_HEARTBEAT = 0x0001; // 客户端心跳
    public static final short PROTO_CLIENT_LOGIN = 0x000E; // 客户端登陆请求（用于绑定后端）
    public static final short PROTO_CLIENT_HEARTBEAT2 = 0x002F; // 客户端心跳

    public static final short PROTO_CLOSE_FRONTEND = 0x7F10; // 关闭客户端连接
    public static final short PROTO_FRONTEND_CLOSED = 0x7F11; // 客户端连接关闭
    public static final short PROTO_UNBIND_BACKEND = 0x7F12; // 解除绑定游戏服连接
    public static final short PROTO_PING = 0x7F13; // 测试消息
    public static final short PROTO_PONG = 0X7F14; // 测试消息
    public static final short PROTO_MODIFY_ROUTE = 0X7F15;//更改玩家数据包的路由
//    public static final short PROTO_CHANGE_BINDING = 0x7F15; // 改变前端连接和后端连接的绑定
    public static final short PROTO_REGISTRATION = 0x7F16; // 战斗服注册包
    public static final short PROTO_PUBLIC_CONFIG = 0x7F17;//公共服的配置

    /* 主服的id */
    public static int MAIN_SERVER_ID;

    /* 连接计数，用于过载保护 */
    public static AtomicInteger payload = new AtomicInteger(0);

    /* 当前最大连接ID，用于生成连接ID */
    public static ThreadLocal<Integer> connectionIdSeq = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    public static ThreadLocal<Map<Integer, FrontendSession>> idToSession = new ThreadLocal<Map<Integer, FrontendSession>>() {
        @Override
        protected Map<Integer, FrontendSession> initialValue() {
            return new HashMap<>();
        }
    };

//    public static ThreadLocal<LinkedArray<BackendSession>> backendSessions = new ThreadLocal<LinkedArray<BackendSession>>() {
//        @Override
//        protected LinkedArray<BackendSession> initialValue() {
//            return new LinkedArray<>(32);
//        }
//    };

    public static ThreadLocal<Map<Integer, BackendSession>> backendSessions = new ThreadLocal<Map<Integer, BackendSession>>() {
        @Override
        protected Map<Integer, BackendSession> initialValue() {
            return new HashMap<>();
        }
    };
    
    public static Map<Integer, Map<Integer, BackendChannelBuildTask>>backendChannelBuildTasks =
    		new ConcurrentHashMap<Integer, Map<Integer,BackendChannelBuildTask>>();

    /* 当前数据统计 */
    public static ThreadLocal<ConnectorStat> currentStat = new ThreadLocal<ConnectorStat>() {
        @Override
        protected ConnectorStat initialValue() {
            return new ConnectorStat();
        }
    };

    /* 累计数据统计 */
    public static ThreadLocal<ConnectorStat> accumulatedStat = new ThreadLocal<ConnectorStat>() {
        @Override
        protected ConnectorStat initialValue() {
            return new ConnectorStat();
        }
    };

    /* 调度器，暂时用于统计任务 */
    public static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /* 连接服配置 */
    public static ConnectorConfig config;

    /* 游戏服配置 */
    public static volatile BackendConfig backendConfig;

    /* 连接服运行标识 */
    public static volatile boolean isRunning = true;

    static {
//        scheduler.scheduleWithFixedDelay(new StatPrintTask(), 0, 5, TimeUnit.MINUTES); // 定时打印数据统计，暂时屏蔽
    }

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    
    
    public Connector(BootstrapConfig bootstrapConfig) {
        super("connector");
        setConfig(bootstrapConfig);
    }

    public static int newConnectionId() {
        int id = connectionIdSeq.get();
        connectionIdSeq.set(id + 1);
        return id;
    }

    public static Map<Integer, FrontendSession> idToSession() {
        return idToSession.get();
    }

    public static FrontendSession getSession(int connectionId) {
        return idToSession.get().get(connectionId);
    }

    public static void putSession(int connectionId, FrontendSession session) {
        idToSession.get().put(connectionId, session);
    }

    public static void removeSession(int connectionId) {
        idToSession.get().remove(connectionId);
    }

    /* backend session */
    public static void setBackendSession(int serverId, BackendSession session) {
        Map<Integer, BackendSession> sessionList = backendSessions.get();
        /* 注意：不允许新值和旧值都不为空的情况
           如果都不为空，旧的连接被替换掉；
             A. 关闭旧连接会造成循环替换，会再次连接新的连接
             B. 不关闭旧连接，会浪费一条连接资源
         */
        BackendSession original = sessionList.get(serverId);
        if (session == null) {
            sessionList.remove(serverId);
        } else if (sessionList.get(serverId) == null) {
            sessionList.put(serverId, session);
        } else {
            // when old value and new value are both not null, there may be a bug here.
        }
        com.stars.util.LogUtil.info("游戏服连接赋值, oldVal={}, newVal={}",
                System.identityHashCode(original), System.identityHashCode(session));
    }

//    public static BackendSession nextBackendSession() {
//        return backendSessions.get().next();
//    }

    public static BackendSession getBackendSession(int serverId) {
        return backendSessions.get().get(serverId);
    }

    


    /**
     * 连接游戏服
     * @param group     Netty IO线程组
     * @param loopSize  Netty IO线程组中的线程个数
     * @param config    游戏服配置
     * @return          是否成功连接（启动的时候，如果启动失败，一直阻塞直到连接成功）
     */
    public boolean connectBackend(EventLoopGroup group, int loopSize, BackendConfig config) {

        final CountDownLatch latch = new CountDownLatch(config.size() * loopSize);
        for (EventExecutor eventLoop : group) {
//        	 public static Map<Integer, Map<Integer, BackendChannelBuildTask>>backendChannelBuildTasks =
//     		new ConcurrentHashMap<Integer, Map<Integer,BackendChannelBuildTask>>();
        	Map<Integer, BackendChannelBuildTask> mapOfEVent = new HashMap<Integer, BackendChannelBuildTask>();
        	backendChannelBuildTasks.put(eventLoop.hashCode(), mapOfEVent);
            for (BackendAddress address : config) {
            	if (mapOfEVent.containsKey(address.getServerId())) {
					continue;
				}
            	BackendChannelBuildTask buildTask = new BackendChannelBuildTask(address, (EventLoop) eventLoop, latch);
                eventLoop.submit(buildTask);
                mapOfEVent.put(address.getServerId(), buildTask);
            }
        }
        // fixme: 临时方案
//        try {
//            latch.await();
//            return true;
//        } catch (InterruptedException e) {
//            return false;
//        }
        return true;
    }

    /**
     * 监听客户端连接
     *
     * @param bossGroup   监听线程（Acceptor）
     * @param workerGroup IO线程组
     * @param ip          监听IP
     * @param port        监听端口
     * @throws Exception
     */
    public void openFrontend(EventLoopGroup bossGroup, EventLoopGroup workerGroup, String ip, int port)
            throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(new LoggingHandler(LogLevel.ERROR))
                .childOption(ChannelOption.SO_SNDBUF, Connector.config.getFrontendSendBuf())
                .childOption(ChannelOption.SO_RCVBUF, Connector.config.getFrontendRecvBuf())
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new PacketDecoder(128 * 1024, 1, 4, 1, 0));
                        ch.pipeline().addLast(new IdleStateHandler(Connector.config.frontendTimeout(), 0, 0));
                        ch.pipeline().addLast(new FrontendInboundHandler());
                    }
                });
        if (ip != null && !"".equals(ip.trim())) {
            bootstrap.bind(ip, port).sync();
        } else {
            bootstrap.bind(port).sync();
        }
    }

    /**
     * 设置统计任务定时运行
     * @param workerGroup   Netty IO线程组
     */
    public void initStat(EventLoopGroup workerGroup) {
        for (EventExecutor loop : workerGroup) {
            loop.scheduleWithFixedDelay(new StatSubmitTask(), 0, 1, TimeUnit.MINUTES);
        }
    }

    /**
     * 加载游戏服配置（配置从配置服加载）
     *
     * @return BackendConfig    游戏服配置
     * @throws Exception
     */
    public BackendConfig loadBackendConfig() throws Exception {
//        ConfigClient.start(getConfig()).get(); // 从配置服读取配置，阻塞等待
//        JsonArray array = new JsonParser().parse(ConfigClient.getConfig())
//                .getAsJsonObject().getAsJsonArray("mainList");
        BackendConfig config0 = new BackendConfig();
        BootstrapConfig config = getConfig();
        // 加载主服配置
        Properties p = config.getProps().get(BootstrapConfig.MAIN);
        if (p == null) {
			p = config.getProps().get("server");
		}
        config0.add(Integer.parseInt(p.getProperty("serverId")), p.getProperty("serverIp"), Integer.parseInt(p.getProperty("serverPort")));
        MAIN_SERVER_ID = Integer.parseInt(p.getProperty("serverId"));
        // 加载战斗服配置
//        p = config.getProps().get(BootstrapConfig.FIGHT);
//        if (p != null && p.size() > 0) {
//            config0.add(Integer.parseInt(p.getProperty("serverId")), p.getProperty("serverIp"), Integer.parseInt(p.getProperty("serverPort")));
//            LogUtil.info("战斗服配置，serverId={}, serverIp={}, serverPort={}",
//                    p.getProperty("serverId"), p.getProperty("serverIp"), p.getProperty("serverPort"));
//        } else {
//            LogUtil.error("缺少战斗服配置");
//        }
        for (Map.Entry<String, Properties> entry : config.getProps().entrySet()) {
            if (entry.getKey().startsWith("main")
                    || entry.getKey().startsWith("connector")
                    || entry.getKey().startsWith("login")
                    || entry.getKey().startsWith("config")
                    || entry.getKey().startsWith("server")) {
                continue;
            } else {
                p = entry.getValue();
                config0.add(Integer.parseInt(p.getProperty("serverId")), p.getProperty("serverIp"), Integer.parseInt(p.getProperty("serverPort")));
                com.stars.util.LogUtil.info("{}配置，serverId={}, serverIp={}, serverPort={}", entry.getKey(),
                        p.getProperty("serverId"), p.getProperty("serverIp"), p.getProperty("serverPort"));
            }
        }

        if (!Connector.config.isTestOn() || Connector.config.needRelayTestPacket()) {
//            Objects.requireNonNull(array, "主服列表为空");
//            for (JsonElement elem : array) {
//                JsonObject jobj = elem.getAsJsonObject();
//                config.add(jobj.get("id").getAsInt(), jobj.get("ip").getAsString(),
//                        jobj.get("port").getAsInt());
//            }
        } else {
            // not to load backend config
        }
        return config0;
    }

    /**
     * 加载连接服配置，主要是IP和端口
     *
     * @param filepath 配置文件路径
     * @throws IOException
     */
    public void loadConfig(String filepath) throws IOException {
        Properties prop = new Properties();
        prop.load(new InputStreamReader(new FileInputStream(filepath), "UTF-8"));
    }

    @Override
    public void start() throws Exception {
        try {
        	com.stars.util.LogUtil.init();
            // 加载配置
        	com.stars.util.LogUtil.info("加载本地配置");
            Connector.config = ConnectorConfig.load(getConfig().getProps().get(BootstrapConfig.CONNECTOR));
            com.stars.util.LogUtil.info("加载游戏服配置");
            Connector.backendConfig = loadBackendConfig();
            com.stars.util.LogUtil.info("配置游戏服共{}个", backendConfig.size());

//            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);

            // 初始化EventLoopGroup
            int loopSize = config.threadNumber();
            com.stars.util.LogUtil.info("初始化线程(worker={})", loopSize);
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup(loopSize);

            com.stars.util.LogUtil.info("连接游戏服...");
            if (connectBackend(workerGroup, loopSize, backendConfig)) {
            	com.stars.util.LogUtil.info("游戏服已连接");
            }
//            initStat(workerGroup); // 设置统计任务定时运行，暂时屏蔽
//            openFrontend(bossGroup, workerGroup, Connector.config.ip(), Connector.config.port());
            openFrontend(bossGroup, workerGroup, null, Connector.config.port());
            com.stars.util.LogUtil.info("监听前端");
            if (config.isOpenBackdoor()) {
                ConnectorBackdoor.init();
                new Thread(new BackdoorServer("127.0.0.1", 52013)).start();
                com.stars.util.LogUtil.info("后门已启动");
            }

            // 路由
            PacketManager.loadCorePacket(); //
//            RouteClient.instance().start();
//            Monitor.registerMonitorHandler(ConnectorMonitorHandler.class);
//            Monitor.registerMonitorHandler(MXBeanHandler.class);

            // 初始化热更机制
            if(!HotUpdateManager.init(LogManager.getLogger("console"), LogManager.getLogger("console"))){
                System.exit(0);
            }

            LogUtil.info("连接服已启动");
        } catch (Exception e) {
            isRunning = false;
            if (bossGroup != null) {
                bossGroup.shutdownGracefully().syncUninterruptibly();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully().syncUninterruptibly();
            }
            throw e;
        }
    }

    @Override
    public void stop() {
        System.out.println("停止连接服...");
        isRunning = false;
        /* 停止线程 */
        System.out.println("停止IO线程...");
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().syncUninterruptibly();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully().syncUninterruptibly();
        }
        /* 完成 */
        System.out.println("连接服已停止");

    }

}
