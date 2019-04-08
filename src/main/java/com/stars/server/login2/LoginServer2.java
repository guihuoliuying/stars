package com.stars.server.login2;

import com.stars.bootstrap.AbstractServer;
import com.stars.network.server.codec.Decrypter;
import com.stars.network.server.codec.Encryptor;
import com.stars.network.server.codec.GamePacketDecoder;
import com.stars.network.server.codec.LengthEncoder;
import com.stars.server.login2.asyncdb.AsyncDbManager;
import com.stars.server.login2.asyncdb.DbObjectState;
import com.stars.server.login2.dbcallback.InsertSqlCallback;
import com.stars.server.login2.dbcallback.UpdateSqlCallback;
import com.stars.server.login2.helper.LHashHelper;
import com.stars.server.login2.model.manager.LAccountManager;
import com.stars.server.login2.model.pojo.LAccount;
import com.stars.server.login2.netty.LoginHandler;
import com.stars.server.login2.sdk.core.LVerifyContextImpl;
import com.stars.server.login2.task.TimeoutTask;
import com.stars.util.ExecuteManager;
import com.stars.util.LogUtil;
import com.stars.util.PropertiesWrapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;

import static com.stars.server.login2.asyncdb.DbObjectState.*;

/**
 * todo: 获取数据连接的类型/属性
 * todo: id的映射关系（3登录服 - 21数据库 - 1000w数据/分区存储）
 * todo: 第三方
 * todo: 加载用户数据
 *
 * Created by zhaowenshuo on 2016/1/29.
 */
public class LoginServer2 extends AbstractServer {

    public static final short PROTO_SERVER_HEARTBEAT = 0x0001; // 心跳
    public static final short PROTO_SERVER_LOGIN_CHECK = 0x0006; // 登录验证请求
    public static final short PROTO_CLIENT_LOGIN_CHECK = 0x0007; // 登录验证响应

    public static volatile com.stars.util.PropertiesWrapper config;

    public static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    public static ConcurrentMap<LVerifyContextImpl, Long> callbackMap = new ConcurrentHashMap<>();

    public static void initScheduler() {
        /* 检查SDK验证超时 */
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                for (Map.Entry<LVerifyContextImpl, Long> en : callbackMap.entrySet()) {
                    try {
                        if (now - en.getValue() > 30000 && callbackMap.remove(en.getKey()) != null) {
                            en.getKey().nettyChannel().eventLoop().submit(new TimeoutTask(en.getKey())); // 丢给event loop线程
                        }
                    } catch (Exception e) {
                        com.stars.util.LogUtil.error("超时检查异常", e);
                    }
                }
            }
        }, 5, 5, TimeUnit.SECONDS);

        /* 定时保存 */
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                for (LAccount account : LAccountManager.valueSet()) {
                    try {
                        synchronized (account) {
                            if (account.getState() != null) {
                                switch (account.getState()) {
                                    case NEW:
                                        account.setState(NEW_SAVING);
                                        String insertSql = account.toInsertSql();
                                        com.stars.util.LogUtil.info("save data: {}", insertSql);
                                        AsyncDbManager.exec(LHashHelper.getDbId(account.getUniqueId()), insertSql,
                                                new InsertSqlCallback(account.getUniqueId()));
                                        break;
                                    case CHANGED:
                                        account.setState(SAVING);
                                        String updateSql = account.toUpdateSqlWithLoginTimestamp();
                                        com.stars.util.LogUtil.info("save data: {}", updateSql);
                                        AsyncDbManager.exec(LHashHelper.getDbId(account.getUniqueId()), updateSql,
                                                new UpdateSqlCallback(account.getUniqueId()));
                                        break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        com.stars.util.LogUtil.error("账号数据定时保存异常", e);
                    }
                }
            }
        }, config.getInt("savingInterval", 5), config.getInt("savingInterval", 5), TimeUnit.SECONDS);

        /* todo:定时清理 */
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                long interval = config.getLong("expired", 24 * 3600);
                for (LAccount account : LAccountManager.valueSet()) {
                    try {
                        synchronized (account) {
                            DbObjectState state = account.getState();
                            if (now - account.getLoginTimestamp().getTime() > interval && state == UNCHANGED) {
                                LAccountManager.remove(account.getUniqueId());
                            }
                        }
                    } catch (Exception e) {
                        com.stars.util.LogUtil.error("账号数据定时清理异常", e);
                    }
                }
            }
        }, 1, 1, TimeUnit.HOURS);
    }

    public static void loadConfig() throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream("./config/login/login_config.properties"));
        LoginServer2.config = new PropertiesWrapper(props);
    }

    public LoginServer2() {
        super("login");
    }

    @Override
    public void start() throws Exception {
    	LogUtil.init();
        ExecuteManager.init(); // 初始化线程池
        init();
        setupNetwork();
    }

    @Override
    public void stop() {

    }

    private void init() throws Exception {
//        loadConfig();
//        LSdkManager.register(0, new TestSdk());
//        LChannelManager.register(0, new LChannel(0, "test"));
//        ExecuteManager.init(Runtime.getRuntime().availableProcessors() * 2);
//        DbUtil.init();
//        LZoneManager.loadData();
//        loadAccountData();
//        AsyncDbManager.init();
//        initScheduler();
    }

    private void loadAccountData() throws Exception {
//        int size = DbUtil.getDataSources().size();
//        CountDownLatch latch = new CountDownLatch(size);
//        for (Long dbId : DbUtil.getDataSources().keySet()) {
//            if (dbId != 99) { // fixme: 怎么样区别产品库
//                ExecuteManager.execute(new LLoadDataTask(dbId, latch)); // todo: 暂不加载
//                latch.countDown(); // 与上一句不能同时启用
//            } else {
//                latch.countDown();
//                size--;
//            }
//        }
//        latch.await(600, TimeUnit.SECONDS); // 等10分钟
//        LHashHelper.sizeOfDatabase = size; // 设置取模的大小
    }

    private void setupNetwork() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(new NioEventLoopGroup(1), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 4096)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                new GamePacketDecoder(Integer.MAX_VALUE, 1, 4, 1, 0),
                                new LengthEncoder(),

                                new Decrypter(),
                                new Encryptor(),

                                new IdleStateHandler(30, 180, 0),

                                new LoginHandler()
                        );
                    }
                });
        bootstrap.bind(config.getInt("port", 9000)).sync();

    }

}
