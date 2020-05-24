package com.stars.startup;

import com.stars.AccountRow;
import com.stars.ServerStatePrinter;
import com.stars.ServerVersion;
import com.stars.SwitchEntranceGm;
import com.stars.bootstrap.SchedulerHelper;
import com.stars.bootstrap.ServerManager;
import com.stars.core.AccessControl;
import com.stars.core.SystemRecordMap;
import com.stars.core.actor.Actor;
import com.stars.core.actor.ActorSystem;
import com.stars.core.actor.invocation.ServiceActor;
import com.stars.core.clientpatch.PatchManager;
import com.stars.core.db.DBUtil;
import com.stars.core.hotupdate.YinHanHotUpdateManager;
import com.stars.core.module.ModuleManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.core.schedule.SchedulerManager;
import com.stars.modules.MConst;
import com.stars.modules.data.DataModuleFactory;
import com.stars.modules.demologin.LoginModuleFactory;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.modules.demologin.message.OfflineMsg;
import com.stars.modules.demologin.packet.ClientReconnect;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.demologin.packet.ServerReconnect;
import com.stars.modules.email.EmailModuleFactory;
import com.stars.modules.friend.FriendModuleFactory;
import com.stars.modules.hotUpdate.HotUpdateModuleFactory;
import com.stars.modules.push.PushModuleFactory;
import com.stars.modules.redpoint.RedPointModuleFactory;
import com.stars.modules.role.RoleModuleFactory;
import com.stars.modules.tool.ToolModuleFactory;
import com.stars.network.PacketChecker;
import com.stars.network.PacketUtil;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.session.GameSession;
import com.stars.server.Business;
import com.stars.server.main.actor.ActorServer;
import com.stars.server.main.message.Disconnected;
import com.stars.services.MainServerServiceManager;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.util.ExecuteManager;
import com.stars.util.LogUtil;
import com.stars.util.ServerLogConst;
import com.stars.util._HashMap;
import com.yinhan.hotupdate.HotUpdateManager;

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhaowenshuo on 2015/3/18.
 */
public class MainStartup implements Business {

    public static boolean isOpenGameGm = false; // 是否开启游戏内GM
    public static ConcurrentMap<String, AccountRow> accountMap = new ConcurrentHashMap<>(); // 账号全局表

    public static String serverChannel = "zyy";

    @Override
    public void init() throws Exception {
        com.stars.util.LogUtil.info("初始化主服业务逻辑...");
        try {
            com.stars.util.ExecuteManager.init(32);
            ServerVersion.load();//加载服务版本号
//            initHotswapEnv();
            DBUtil.init();// 初始化数据库连接池(proxool)
            checkPacket(); // 检查协议号，是否全局唯一
            SchedulerHelper.init("./config/jobs/quartz.properties");
            SchedulerManager.init(SchedulerManager.scheduledCorePoolSize);
            initModule(); // 初始化模块
//            loadProductData(); // 加载数据(产品数据)
//            loadSystemRecordMap();
            ActorServer.setActorSystem(new ActorSystem()); // 初始化ActorSystem
            PlayerSystem.init();
            ServiceSystem.init();
            YinHanHotUpdateManager.init();
            ServiceHelper.init(new MainServerServiceManager());
            SchedulerHelper.start();
            SchedulerManager.initScheduler();
//            //输出资源加载文件列表
//            ResoucePrinter.getInstance().writeResourceList();
            PatchManager.init();
            initGameServerConfig(); // 初始化服务入口
            ServerStatePrinter.init();
        } catch (Throwable cause) {
            com.stars.util.LogUtil.error("MainStartup.init() error ", cause);
            System.exit(1);
        }
    }

    private void initDistEnv() throws Exception {
        // TODO 初始化分布式锁
    }

    public static void initHotswapEnv() throws Exception {
        if (!HotUpdateManager.init(ServerLogConst.console, ServerLogConst.exception)) {
            System.exit(0);
        }
        HotUpdateManager.checkClassFile();
        //热更回调设置
//        HotUpdateManager.setCallBackClassName("com.stars.manager.ServerManager.ServerManagerDataPool");
//        HotUpdateManager.setCallBackMethod("addHotUpdateCount");
    }


    /**
     * 初始化业务模块
     * 依赖模块必须放在前面,优先初始化
     *
     * @throws Exception
     */
    private void initModule() throws Exception {
        ModuleManager.register(MConst.Login, new LoginModuleFactory()); // 登录 -
        ModuleManager.register(MConst.HotUpdate, new HotUpdateModuleFactory()); // 热更模块(处理线上问题)
        ModuleManager.register(MConst.RedPoint, new RedPointModuleFactory()); // 红点
        ModuleManager.register(MConst.Data, new DataModuleFactory());// 通用配置数据commondefine表 OK
        ModuleManager.register(MConst.Push, new PushModuleFactory()); // 精准推送 OK
        ModuleManager.register(MConst.Role, new RoleModuleFactory()); // 人物 OK
        ModuleManager.register(MConst.Tool, new ToolModuleFactory()); // 道具/背包 OK
        ModuleManager.register(MConst.Email, new EmailModuleFactory()); // 邮件 OK -
        ModuleManager.register(MConst.Friend, new FriendModuleFactory()); // 好友 OK -
        ModuleManager.initDependence();
        ModuleManager.initPacket(); // 初始化数据包
        ModuleManager.init(); // 模块初始化
        com.stars.util.LogUtil.info("完成模块注册");
    }


    private void checkPacket() throws Exception {
        PacketChecker.check();
    }


    private void loadProductData() throws Exception {
        ModuleManager.loadProductData();
    }

    private void loadSystemRecordMap() throws Throwable {
        SystemRecordMap.load();
        // 启动时检查每日重置
        if (System.currentTimeMillis() - SystemRecordMap.dailyResetTimestamp > 24 * 3600 * 1000) {
            com.stars.util.LogUtil.info("启动时每日重置");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR, 0);

            SystemRecordMap.update("dailyResetTimestamp", calendar.getTimeInMillis());

//            calendar.set(Calendar.MILLISECOND, 0);
//            calendar.set(Calendar.SECOND, 0);
//            calendar.set(Calendar.MINUTE, 0);
//            calendar.set(Calendar.HOUR, 0);
            SystemRecordMap.update("dateVersion", (int) (calendar.getTimeInMillis() / 1000));
        }

        // 启动时检查每日重置
        if (System.currentTimeMillis() - SystemRecordMap.fiveOClockResetTimestamp > 24 * 3600 * 1000) {
            com.stars.util.LogUtil.info("启动时每日五点重置");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR, 5);
            SystemRecordMap.update("fiveOClockResetTimestamp", calendar.getTimeInMillis());
        }
    }

    @Override
    public void clear() {

    }


    @Override
    public void dispatch(final com.stars.network.server.packet.Packet packet) {
//        LogUtil.info("收到协议，roleId={}, packetType=0x{}", packet.getRoleId(), Integer.toHexString(packet.getType()));
        /* 访问控制 */
        if (!AccessControl.canAccess(packet.getType())) {
            com.stars.network.server.packet.PacketManager.send(packet.getSession(), new ClientText("功能暂未开放"));
            return;
        }
//        if (RpcManager.handlePacket(packet)) {
//            return;
//        }
        if (packet instanceof ServerReconnect) {//断线重连处理
            ServerReconnect serverReconnect = (ServerReconnect) packet;
            long reconnectRoleId = serverReconnect.getRoleId();
            if (reconnectRoleId != 0) {
                packet.getSession().setRoleId(reconnectRoleId);
            } else { // 选角 & 创角界面不进行断线重连

                return;
            }
        }
        if (packet instanceof com.stars.server.main.message.Disconnected) {
            String accountName = packet.getSession().getAccount();
            if (accountName == null) {
                return;
            }
            com.stars.util.LogUtil.info("发送Disconnected0, account={}", accountName);
            try {
                if (packet.getRoleId() != 0) { // 须比较session（如果存在比较角色的情况下）
                    com.stars.util.LogUtil.info("发送Disconnected1, account={}", accountName);
                    execByPlayer(new OfflineMsg(packet.getRoleId(), packet.getSession()));
                }
            } catch (Throwable t) {
                com.stars.util.LogUtil.error("", t);
            }
        } else {
            final com.stars.network.server.session.GameSession session = packet.getSession();
            if (session != null) {
                if (session.getRoleId() == 0) {
                    execByThread(session, packet);
                } else {
                    // 判断
                    ServiceActor service = ServiceHelper.serviceMapByPacketType.get(packet.getType());
                    if (service != null) {
                        service.tell(packet, com.stars.core.actor.Actor.noSender);
                    } else {
                        execByPlayer(packet);
                    }
                }
            }
        }
    }


    private void execByThread(final GameSession session, final com.stars.network.server.packet.Packet packet) {
        ExecuteManager.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    packet.execPacket();
                } catch (Exception e) {
                    if (!session.isServerSession()) {
                        PacketUtil.error(session);
                    }
                    com.stars.util.LogUtil.error("packet execute error roleid:" + session.getRoleId(), e);
                }
            }
        });
    }

    private boolean execByPlayer(final Packet packet) {
        Player player = PlayerSystem.get(packet.getRoleId());
        if (player != null) {
            if (packet instanceof Disconnected) {
                LogUtil.info("发送Disconnected4, roleId={}", packet.getRoleId());
            }
            player.tell(packet, Actor.noSender);
            return true;
        }
        if (packet instanceof OfflineMsg) {
            ((OfflineMsg) packet).finish(true);
        }
        if (packet instanceof ServerReconnect) {
            com.stars.network.server.packet.PacketManager.send(packet.getSession(), new ClientReconnect(false));
        }
        return false;
    }

    private void loadServerChannel() {
        try {
            _HashMap map = DBUtil.querySingleMap(DBUtil.DB_LOGIN, "select * from common where common.key='serverchannel'");
            if (map != null && map.size() >= 1) {
                String str = (String) map.get("common.value");
                serverChannel = str.trim();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void initGameServerConfig() {
        Properties prop = com.stars.bootstrap.ServerManager.getServer().getConfig().getProps().get(
                ServerManager.getServer().getConfig().getServer());
        // 游戏服入口
        String openServer = prop.getProperty("openServer");
        if (openServer != null && openServer.equals("true")) {
            LoginModuleHelper.serverState = SwitchEntranceGm.OPEN;
        } else {
            LoginModuleHelper.serverState = SwitchEntranceGm.CLOSE;
        }
        // 游戏服GM
        String openGameGm = prop.getProperty("openGameGm");
        if (openGameGm != null && openGameGm.equals("true")) {
            isOpenGameGm = true;
        } else {
            isOpenGameGm = false;
        }
    }

}
