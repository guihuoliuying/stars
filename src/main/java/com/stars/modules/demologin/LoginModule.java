package com.stars.modules.demologin;

import com.stars.AccountRow;
import com.stars.core.SystemRecordMap;
import com.stars.core.db.DBUtil;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Guard;
import com.stars.core.module.Module;
import com.stars.core.persist.SaveDBManager;
import com.stars.core.persist.SaveDbResult;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.core.recordmap.RoleRecord;
import com.stars.core.recordmap.RoleRecordMapImpl;
import com.stars.modules.MConst;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.event.LoginSuccessEvent;
import com.stars.modules.demologin.gm.PrintPacketGmHandler;
import com.stars.modules.demologin.message.*;
import com.stars.modules.demologin.packet.*;
import com.stars.modules.demologin.userdata.AccountRole;
import com.stars.modules.demologin.userdata.LoginInfo;
import com.stars.modules.demologin.userdata.LoginRow;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.role.RoleModule;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.network.server.session.GameSession;
import com.stars.network.server.session.SessionManager;
import com.stars.services.ServiceHelper;
import com.stars.services.summary.SummaryComponent;
import com.stars.startup.MainStartup;
import com.stars.util.LogUtil;
import com.stars.util.ServerLogConst;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by liuyuheng on 2016/6/16.
 */
public class LoginModule extends AbstractModule implements Guard {
    /* 状态简化成两个（在线/离线） */
    private final static int OFFLINE = 0;
    private final static int ONLINE = 1;

    private LoginRow loginRow;
    private int state = OFFLINE; // 状态, 初始为离线状态
    private long offlineTimestamp; // 下线时间戳
    private long heartbeatTimestamp; // 心跳时间戳

    private com.stars.network.server.session.GameSession session;
    private String account;
    private long roleId;
    private String roleName;

    public String snapChannelId;//临时渠道,给充值时玩家离线用的，其他地方不要用

    private Map<Integer, Long> packetIdTimeStamp;

    /* 自动保存相关（备份） */
    private long savingTimestamp;

    private long loginTime;

    public LoginModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("登录", id, self, eventDispatcher, moduleMap);
    }

    public String getAccount() {
        return account;
    }

    public long getRoleId() {
        return roleId;
    }

    public String getInfoString() {
        return "account = " + account + " , roleId = " + roleId;
    }

    public void setHeartbeatTimestamp(long heartbeatTimestamp) {
        this.heartbeatTimestamp = heartbeatTimestamp;
    }

    public long getLastLoginTimestamp() {
        return loginRow.getLastLoginTimestamp();
    }

    public long getLastLastLoginTimestamp() {
        return loginRow.getLastLastLoginTimestamp();
    }

    public void handle(Object message, Player player) {
        /* 登录 */
        if (message instanceof LoginMsg) {
            handleLoginMsg((LoginMsg) message);

        /* 挤号 */
        } else if (message instanceof SqueezeMsg) {
            handleSqueezeMsg((SqueezeMsg) message);

        /* 玩家下线 */
        } else if (message instanceof OfflineMsg) {
            handleOfflineMsg((OfflineMsg) message);

        /* 踢下线 */
        } else if (message instanceof KickOffMsg) {
            handleKickOffMsg((KickOffMsg) message);

        /* 自动保存 */
        } else if (message instanceof AutoSaveMsg) { // 消息
            handleAutoSaveMsg(((AutoSaveMsg) message).getResult());

        /* 每日重置 */
        } else if (message instanceof DailyResetMsg) {
            handleDailyResetMsg(false);

        /* 每周重置 */
        } else if (message instanceof WeeklyResetMsg) {
            handleWeeklyResetMsg(false);

        /* 每月重置 */
        } else if (message instanceof MonthlyResetMsg) {
            handleMonthlyResetMsg();

        /* 定时执行 */
        } else if (message instanceof TimingExecuteMessage) {
            handleTimingExecute();

        /* 每日凌晨五点重置 */
        } else if (message instanceof FiveOClockResetMsg) {
            handleFiveOClockResetMsg();
        } else if (message instanceof LogExitMessage) {
            logExit();
        } else if (message instanceof PullUpMsg) {
            handlePullUpMsg((PullUpMsg) message);
        }
    }

    public int getAccountRoleCount() throws SQLException {
        AccountRow accountRow = LoginModuleHelper.getOrLoadAccount(this.account, null);
        if (accountRow != null) {
            return accountRow.getRelativeRoleList().size();
        }
        return 0;
    }

    public AccountRow getAccountRow() throws SQLException {
        return LoginModuleHelper.getOrLoadAccount(this.account, null);
    }

    private void setupSession(long roleId, String account, com.stars.network.server.session.GameSession session) {
        /* 先顶号 */
        if (this.session != null) {
            handleSqueezeMsg(new SqueezeMsg()); // fixme:
            this.state = ONLINE; // 因为挤号会把状态设置为OFFLINE，所以这里需要设置为ONLINE
        }

        this.roleId = roleId;
        this.account = account;
        this.session = session;
        this.session.setAccount(account);
        this.session.setRoleId(roleId);
        self().session(session);
    }

    private void setupSession(com.stars.network.server.session.GameSession session) {
        this.session = session;
        self().session(session);
        oldSessionCheckAndClose();
        com.stars.network.server.session.SessionManager.put(roleId, session);
    }

    private void setupSession(long roleId, String account) {
        this.roleId = roleId;
        this.account = account;
    }

    private void oldSessionCheckAndClose() {
        com.stars.network.server.session.GameSession gs = com.stars.network.server.session.SessionManager.getSessionMap().get(roleId);
        if (gs == null) return;
        gs.setRoleId(0);
        com.stars.network.server.packet.PacketManager.closeFrontend(gs);
    }

    private void handlePullUpMsg(PullUpMsg message) {
        try {
            loginTime = System.currentTimeMillis();
            state = ONLINE;
            self().resetResendData(); // 重置重发状态和重发队列
            eventDispatcher().disable(); // 关闭事件分发器
            setupSession(message.getRoleId(), message.getAccount()); // 设置session
//            squeezeAndSetCurrentRoleId(roleId, message.getRoleIdVersion()); // 挤号，并设置当前的currentRoleId
            sendLog(message.getAccount()); // fixme: bug here
            fireInitForPay(message.isCreation(), message.getSnapChannelId()); // 初始化
            sendLog(message.getAccount()); // fixme: bug here
            eventDispatcher().enable(); // 开启事件分发器
            // -------
            resetWhileLogin(); // 重置
            updateSummaryWhileLogin(); // 更新摘要数据
            fireSyncData(); // 跟客户端同步数据
//            logLogin(message); //登陆日志
            offlineTimestamp = System.currentTimeMillis();
//            send(new ClientLogin(true)); // 发送登录成功响应包
//            eventDispatcher().fire(new LoginSuccessEvent()); // 抛出登录成功事件
//            eventDispatcher().fire(new DailyFuntionEvent(DailyManager.DAILYID_LOGININ, 1)); // 抛出日常活动事件
            finishMessage(message, true); // 完成登录

        } catch (Throwable t) {
            state = OFFLINE;
//            fireOffline();
            finishMessage(message, false);
            com.stars.util.LogUtil.error("拉起过程出错[支付时离线], account=" + account + ", roleId=" + roleId, t);
        }
    }

    private void handleLoginMsg(LoginMsg message) {
        try {
            if (session == message.getSession()) { // 预防两次点击进入游戏，还是自己切换自己的情况
                return;
            }
            loginTime = System.currentTimeMillis();
            state = ONLINE;
            self().resetResendData(); // 重置重发状态和重发队列
            eventDispatcher().disable(); // 关闭事件分发器
            setupSession(message.getRoleId(), message.getAccount(), message.getSession()); // 设置session
            squeezeAndSetCurrentRoleId(roleId, message.getRoleIdVersion()); // 挤号，并设置当前的currentRoleId
            sendLog(message.getAccount()); // fixme: bug here
            fireInit(message.isCreation()); // 初始化
            sendLog(message.getAccount()); // fixme: bug here
            eventDispatcher().enable(); // 开启事件分发器
            // -------
            AccountRow accountRow = LoginModuleHelper.getOrLoadAccount(account, null);
            resetWhileLogin(); // 重置
            updateSummaryWhileLogin(); // 更新摘要数据
            fireSyncData(); // 跟客户端同步数据
            logLogin(message); //登陆日志
            offlineTimestamp = System.currentTimeMillis();
            send(new ClientLogin(true)); // 发送登录成功响应包
            eventDispatcher().fire(new LoginSuccessEvent()); // 抛出登录成功事件
            eventDispatcher().fire(new DailyFuntionEvent(DailyManager.DAILYID_LOGININ, 1)); // 抛出日常活动事件
            finishMessage(message, true); // 完成登录

        } catch (Throwable t) {
            state = OFFLINE;
            fireOffline();
            finishMessage(message, false);
            com.stars.util.LogUtil.error("登录过程出错, account=" + account + ", roleId=" + roleId, t);
        }
    }

    private void handleSqueezeMsg(SqueezeMsg message) {
        try {
            state = OFFLINE;
            offlineTimestamp = System.currentTimeMillis();
            fireOffline();
            com.stars.util.LogUtil.info("账号:[{}]挤号", account);
            if (session != null) {
                ClientReconnect client = new ClientReconnect(false);
                client.setReason((byte) 1);
                com.stars.network.server.packet.PacketManager.send(session, client);
                com.stars.network.server.packet.PacketManager.closeFrontend(session);
            }
            finishMessage(message, true);
            logExit();
        } catch (Throwable t) {
            finishMessage(message, false);
        } finally {
            setCurrentRoleIdToZero();
        }
    }

    private void handleOfflineMsg(OfflineMsg message) {
        if (message != null && message.getSession() != session) {
            com.stars.util.LogUtil.info("忽略OfflineMsg, account={}, self.session={}, msg.session={}", account, session, message.getSession());
            return;
        }
        try {
            if (state == ONLINE) { // 在线: 修改状态; 离线/初始化: 默认成功
                offlineTimestamp = System.currentTimeMillis();
                fireOffline();
                com.stars.util.LogUtil.info("账号:[{}]离线,roleid={},rolename={}", account, roleId, roleName);
                com.stars.network.server.session.SessionManager.remove(id(), session); // 移除roleId和session的对应关系
                session = null;
                self().getLazyQueue().clear(); // 移除延迟发包队列的数据
            }
            finishMessage(message, true);
        } catch (Throwable t) {
            finishMessage(message, false);
        } finally {
            state = OFFLINE;
            com.stars.util.LogUtil.info("玩家离线, account={}, roleId={}, state={}", account, roleId, state);
            setCurrentRoleIdToZero();
        }
    }

    public void handleOfflineMsgFromGM() {
        handleOfflineMsg(null);
    }

    private void handleKickOffMsg(KickOffMsg message) {
        // 1.触发执行模块onOffline
        // 2.离线保存
        // 3.立即释放资源onExit
        // 4.立即移除内存，停止actor
        try {
            state = OFFLINE;
            offlineTimestamp = System.currentTimeMillis();
            fireOffline();
            com.stars.util.LogUtil.info("踢玩家下线, account={}, sizeOfActorSystem={},name={},roleid={}", account, PlayerSystem.size(), roleName, roleId);
            com.stars.network.server.packet.PacketManager.send(session, new ClientText("你已被GM踢下线,请尝试重新登陆游戏"));
            ClientReconnect client = new ClientReconnect(false);
            client.setReason((byte) 2);//被踢下线
            com.stars.network.server.packet.PacketManager.send(session, client);
            if (session != null) {
                com.stars.network.server.packet.PacketManager.closeFrontend(session);
            }
            saveData();
            if (!context().isSavingSucceeded()) {
//                LogUtil.info("移除玩家数据失败, account={}, sizeOfActorSystem={}, sqlList={}",
//                        account, PlayerSystem.size(), context().getSqlList());
                List<String> sqlList = context().getSqlList();
                for (String sql : sqlList) {
                    if (sql != null && !sql.equals("")) {
                        ServerLogConst.sql_err.info(sql);
                    }
                }
            }
            fireExit();
            self().stop();
            com.stars.util.LogUtil.info("移除玩家数据, account={}, sizeOfActorSystem={}", account, PlayerSystem.size());
            finishMessage((LoginSyncMsg) message, true);
            logExit();


        } catch (Throwable t) {
            com.stars.util.LogUtil.info("踢玩家下线失败, 保存数据异常", t);
            finishMessage((LoginSyncMsg) message, false);
        } finally {
            setCurrentRoleIdToZero();
        }
    }

    private void handleAutoSaveMsg(SaveDbResult result) {
        try {
//            LogUtil.info("执行自动保存[开始],account={}，roleId={}, state={}", account, roleId, state);
            if (state == OFFLINE
                    && System.currentTimeMillis() - offlineTimestamp > 600_000
                    && context().isSavingSucceeded()) {
                // 离线情况:数据已经保存,间隔时间超过阀值,则释放资源,移除内存
                try {
//                    LogUtil.info("移除玩家数据[开始],account={}，roleId={}", account, roleId);
                    fireExit(); // 下线前释放资源
                    logExit();
                } finally {
                    self().stop(); // 停止自身（包括移除内存数据）
                    SaveDBManager.removeRole(id());
                    com.stars.util.LogUtil.info("移除玩家数据, account={}, roleId={}, sizeOfActorSystem={}", account, roleId, PlayerSystem.size());
                }
                return;
            }
            // 其余情况自动保存
//            LogUtil.info("执行自动保存[开始],account={}，roleId={}", account, roleId);
            saveData();
//            LogUtil.info("执行自动保存,account={}，roleId={}, state={}", account, roleId, state);
        } catch (Throwable e) {
            com.stars.util.LogUtil.error("执行自动保存异常, account={}，roleId={}, state={}", account, roleId, state);
            com.stars.util.LogUtil.error(e.getMessage(), e);
        } finally {
            if (result != null) {
                if (result.finish()) {
                    result.getCounter().decrementAndGet();
                }
            }
        }

    }

    /**
     * 检查每日重置，和进行每日重置；应满足幂等操作（每日重置只能发挥一次作用）
     */
    private void handleDailyResetMsg(boolean isLogin) {
        long resetTimestamp = SystemRecordMap.dailyResetTimestamp;
        if (loginRow.getLastDailyResetTimestamp() < resetTimestamp) {
            try {
                Calendar now = Calendar.getInstance();
                fireDailyReset(now, isLogin);
            } catch (Throwable cause) {
                com.stars.util.LogUtil.error("", cause);
            }
            loginRow.setLastDailyResetTimestamp(resetTimestamp);
            context().update(loginRow);
        }
    }

    /**
     * 检测每日凌晨五点重置
     */
    private void handleFiveOClockResetMsg() {
        long resetTimestamp = SystemRecordMap.fiveOClockResetTimestamp;
        if (loginRow.getLastFiveOClockResetTimestamp() < resetTimestamp) {
            try {
                Calendar now = Calendar.getInstance();
                fireFiveOClockReset(now);
            } catch (Throwable cause) {
                com.stars.util.LogUtil.error("", cause);
            }
            loginRow.setLastFiveOClockResetTimestamp(resetTimestamp);
            context().update(loginRow);
        }
    }

    private void handleWeeklyResetMsg(boolean isLogin) {
        long resetTimestamp = SystemRecordMap.weeklyResetTimestamp;
        if (resetTimestamp != 0 && loginRow.getLastWeeklyResetTimestamp() < resetTimestamp) {
            try {
                fireWeeklyReset(isLogin);
            } catch (Throwable cause) {
                com.stars.util.LogUtil.error("", cause);
            }
            loginRow.setLastWeeklyResetTimestamp(resetTimestamp);
            context().update(loginRow);
        }
    }

    private void handleMonthlyResetMsg() {
        long resetTimestamp = SystemRecordMap.monthlyResetTimestamp;
        if (loginRow.getLastMonthlyResetTimestamp() < resetTimestamp) {
            try {
                fireMonthlyReset();
            } catch (Throwable cause) {
                com.stars.util.LogUtil.error("", cause);
            }
            loginRow.setLastMonthlyResetTimestamp(resetTimestamp);
            context().update(loginRow);
        }
    }

    private void handleTimingExecute() {
        for (Module m : moduleMap().values()) {
            try {
                m.onTimingExecute();
            } catch (Exception e) {
                com.stars.util.LogUtil.error("模块[{}]用户定时执行异常: roleId={}, message={}", m.name(), getRoleId(), e.getMessage());
                com.stars.util.LogUtil.error("", e);
            }
        }
    }

    public void succeedReconnect() {
        state = ONLINE;
        heartbeatTimestamp = System.currentTimeMillis();
        ClientReconnect packet = new ClientReconnect(true);
        packet.setMaxServerPacketId(self().getMaxServerPacketId());
        send(packet);
    }

    @Override
    public void onInit(boolean isCreation) {
        packetIdTimeStamp = new LinkedHashMap<>();
        heartbeatTimestamp = System.currentTimeMillis();
        updateRelativeLoginRowTime();
        loginRow.setLastLoginTimestamp(System.currentTimeMillis());
        context().update(loginRow);
        try {
            AccountRow accountRow = LoginModuleHelper.getOrLoadAccount(account, null);
            AccountRole accountRole = accountRow.getAccountRole(id());
            if (accountRole != null) {
                accountRole.lastLoginTimestamp = loginRow.getLastLoginTimestamp();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * 修改级联对象
     * 实际上，角色登陆会存在两份loginrow对象 一份是loginmodulehelper中查询 一份是loginmodule中查询
     * 前者精准推送表达式会用到
     */
    private void updateRelativeLoginRowTime() {
        AccountRow accountRow = null;
        try {
            accountRow = getAccountRow();
            if (accountRow.getLoginLock().tryLock(500, TimeUnit.MILLISECONDS)) {
                Map<Long, LoginRow> loginRowMap = accountRow.getLoginRowMap();
                if (loginRowMap != null) {
                    LoginRow loginRow = loginRowMap.get(id());
                    if (loginRow != null) {
                        loginRow.setLastLoginTimestamp(System.currentTimeMillis());
                    }
                }
            }
        } catch (SQLException e) {
            com.stars.util.LogUtil.error(e.getMessage(), e);
        } catch (Exception e) {
            com.stars.util.LogUtil.error(e.getMessage(), e);
        } finally {
            accountRow.getLoginLock().unlock();
        }
    }

    @Override
    public void onReconnect() throws Throwable {

    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        send(new ClientServerDate(openServerDate()));
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from `login` where `roleid`=" + id();
        loginRow = DBUtil.queryBean(DBUtil.DB_USER, LoginRow.class, sql);

        // 获取玩家的键值对记录
        sql = "select * from `rolerecords` where `roleid`=" + id();
        Map<String, RoleRecord> map = DBUtil.queryMap(DBUtil.DB_USER, "recordkey", RoleRecord.class, sql);
        if (map == null) {
            map = new HashMap<>();
        }
        context().recordMap(new RoleRecordMapImpl(id(), context(), map));
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        loginRow = new LoginRow(id());
        this.account = account; // 特殊处理
        context().insert(loginRow);
        context().recordMap(new RoleRecordMapImpl(id(), context(), new HashMap<String, RoleRecord>()));
        AccountRow accountRow = getAccountRow();
        Map<Long, LoginRow> loginRowMap = accountRow.getLoginRowMap();
        loginRowMap.put(id(), loginRow);
    }

    @Override
    public void onOffline() throws Throwable {
        ServiceHelper.summaryService().offline(id());
    }

    @Override
    public void onTimingExecute() {
        long now = System.currentTimeMillis();
        // 不活跃超时检查（依据心跳，防止Disconnected丢失的情况，3600秒）
        if (state == ONLINE && now - heartbeatTimestamp > 3_600_000) {
            com.stars.util.LogUtil.info("不活跃超时下线, account={}, roleId={}", account, roleId);
            try {
                if (session != null) {
                    com.stars.network.server.packet.PacketManager.closeFrontend(session); // 如果session不为空，则关闭连接
                }
                handleOfflineMsg(null);
            } catch (Throwable t) {
                com.stars.util.LogUtil.error(t.getMessage(), t);
            }
        }
        // 自动保存
        if (now - savingTimestamp > 60_000) {
            if (SaveDBManager.enableBackupSaving) {
                if (now - savingTimestamp > 120_000 || (now & id() & 1) == 1) {
                    if (SaveDBManager.backupCurrentSavingSemaphore.tryAcquire()) {
                        try {
                            savingTimestamp = now;
                            handleAutoSaveMsg(null);
                        } catch (Throwable t) {
                            com.stars.util.LogUtil.error("后备保存方案异常", t);
                        } finally {
                            SaveDBManager.backupCurrentSavingSemaphore.release();
                        }
                    }
                }
            } else {
                savingTimestamp = now;
            }
        }
        // 延迟发包
        if (self().getLazyQueue().size() > 0) {
            Queue<com.stars.network.server.packet.Packet> lazyQueue = self().getLazyQueue();
            for (int count = 64; count > 0; count--) {
                try {
                    com.stars.network.server.packet.Packet packet = lazyQueue.poll();
                    if (packet != null) {
                        send(packet);
                    }
                } catch (Throwable t) {
                    com.stars.util.LogUtil.error(t.getMessage(), t);
                }
            }
        }
    }

    private int openServerDate() {
        return DataManager.getServerDays();
    }

    /* 模块相关操作 */

    /**
     * 模块加载后将基本属性发送到serverLog模块
     */
    public void sendLog(String acocunt) {
        try {
            for (Module m : moduleMap().values()) {
                try {
                    m.onLog();
                } catch (Throwable t) {
                    com.stars.util.LogUtil.error("onLog异常, roleId=" + id() + ", moduleName=" + m.name(), t);
                }
            }
        } catch (Throwable t) {
            ServerLogConst.exception.info(t.getMessage());
        }
    }

    public void fireCreation(String name, String account) throws Throwable {
        for (Module m : moduleMap().values()) {
            m.onCreation(name, account);
        }
    }

    public void fireLoadData() throws Throwable {
        for (Module m : moduleMap().values()) {
            m.onDataReq();
        }
    }

    public void fireInit(boolean isCreation) throws Throwable {
        for (Module m : moduleMap().values()) {
            m.onInit(isCreation);
        }
    }

    private void fireInitForPay(boolean isCreation, int snapChannelId) throws Throwable {
        for (Module m : moduleMap().values()) {
            m.onInit(isCreation);
            if (m instanceof LoginModule) {
                ((LoginModule) m).snapChannelId = Integer.toString(snapChannelId);
            }
        }
    }

    public void fireReconnect() throws Throwable {
        for (Module m : moduleMap().values()) {
            try {
                m.onReconnect();
            } catch (Throwable t) {
                com.stars.util.LogUtil.error("onReconnect异常, roleId=" + id() + ", moduleName=" + m.name(), t);
            }
        }
    }

    private void updateSummaryWhileLogin() throws Throwable {
        ServiceHelper.summaryService().online(roleId);
        fireUpdateSummary();
    }

    public void fireOffline() {
        for (Module m : moduleMap().values()) {
            try {
                m.onOffline();
            } catch (Throwable t) {
                com.stars.util.LogUtil.error("onOffline异常, roleId=" + id() + ", moduleName=" + m.name(), t);
            }
        }
    }

    public void fireExit() {
        for (Module m : moduleMap().values()) {
            try {
                m.onExit();
            } catch (Throwable t) {
                com.stars.util.LogUtil.error("onExit, roleId=" + id() + ", moduleName=" + m.name(), t);
            }
        }
    }

    public void fireDailyReset(Calendar now, boolean isLogin) {
        for (Module m : moduleMap().values()) {
            try {
                m.onDailyReset(now, isLogin);
            } catch (Throwable t) {
                com.stars.util.LogUtil.error("onDailyReset异常, roleId=" + id() + ", moduleName=" + m.name(), t);
            }
        }
    }

    public void fireFiveOClockReset(Calendar now) {
        for (Module m : moduleMap().values()) {
            try {
                m.onFiveOClockReset(now);
            } catch (Throwable t) {
                com.stars.util.LogUtil.error("onFiveOClockReset, roleId=" + id() + ", moduleName=" + m.name(), t);
            }
        }
    }

    public void fireWeeklyReset(boolean isLogin) {
        for (Module m : moduleMap().values()) {
            try {
                m.onWeeklyReset(isLogin);
            } catch (Throwable t) {
                com.stars.util.LogUtil.error("onWeeklyReset异常, roleId=" + id() + ", moduleName=" + m.name(), t);
            }
        }
    }

    public void fireMonthlyReset() {
        for (Module m : moduleMap().values()) {
            try {
                m.onMonthlyReset();
            } catch (Throwable t) {
                com.stars.util.LogUtil.error("onMonthlyReset异常, roleId=" + id() + ", moduleName=" + m.name(), t);
            }
        }
    }


    public void fireSyncData() {
        for (Module m : moduleMap().values()) {
            try {
                m.onSyncData();
            } catch (Throwable t) {
                com.stars.util.LogUtil.error("onSyncData异常, roleId=" + id() + ", moduleName=" + m.name(), t);
            }
        }
    }

    public void fireUpdateSummary() {
        Map<String, SummaryComponent> map = new HashMap<>();
        for (Module m : moduleMap().values()) {
            try {
                m.onUpateSummary(map);
            } catch (Throwable t) {
                com.stars.util.LogUtil.error("onUpateSummary异常, roleId=" + id() + ", moduleName=" + m.name(), t);
            }
        }
        try {
            ServiceHelper.summaryService().updateSummaryComponent(id(), map);
        } catch (Throwable t) {
            com.stars.util.LogUtil.error("fireUpdateSummary异常, roleId=" + id(), t);
        }
    }

    private void resetWhileLogin() {
        handleDailyResetMsg(true);
        handleFiveOClockResetMsg();
        handleWeeklyResetMsg(true);
        handleMonthlyResetMsg();
    }

    private void saveData() {
        context().flush();
    }

    private void finishMessage(LoginSyncMsg message, boolean isSucceeded) {
        if (message != null) {
            message.finish(isSucceeded);
        }
    }


    @Override
    public boolean canAccess(Packet packet) {
        if (PrintPacketGmHandler.canPrintPacket(String.format("0x%04X", packet.getType())))
            com.stars.util.LogUtil.info("roleId:{} 收到客户端的packet|packetId:{}", id(), packet.getPacketId());
        if (packetIdTimeStamp == null) {
            return true;
        }
        if (!packetIdTimeStamp.containsKey(packet.getPacketId())) {
            return true;
        }
        return (System.currentTimeMillis() - packetIdTimeStamp.get(packet.getPacketId())) / 1000 >= 2;
    }

    @Override
    public void onCallAccess(int pid) {
        if (packetIdTimeStamp == null || pid == 0) return;
        packetIdTimeStamp.put(pid, System.currentTimeMillis());
        List<Integer> packetIdList = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : packetIdTimeStamp.entrySet()) {
            if ((System.currentTimeMillis() - entry.getValue()) / 1000 >= 3) {
                packetIdList.add(entry.getKey());
            }
        }
        for (int packetId : packetIdList) {
            packetIdTimeStamp.remove(packetId);
        }
    }

    @Override
    public void onLog() {

    }

    private void logLogin(LoginMsg message) throws SQLException {
        AccountRow accountRow = LoginModuleHelper.getOrLoadAccount(message.getAccount(), null);
        LoginInfo loginInfo = accountRow.getLoginInfo();
        Player player = PlayerSystem.get(message.getRoleId());
        if (player != null && player.isLogExit()) {
            player.setLogExit(false);
        }
        if (loginInfo != null) {
            accountRow.setLastLogLoginTime(System.currentTimeMillis());
            String time = accountRow.getRelativeRoleTime(message.getRoleId() + "");
        }
    }

    private void logRoleExit() throws SQLException {
        AccountRow accountRow = LoginModuleHelper.getOrLoadAccount(account, null);
        LoginInfo loginInfo = accountRow.getLoginInfo();
        //online_time:本次在线时长（单位秒）（整数）#main_coin:充值币持有#gift_coin:赠送充值币持有#sub_coin:游戏币持有sp_case:普通关卡驻留#nm_case:精英关卡驻留
        String time = accountRow.getRelativeRoleTime(id() + "");
        RoleModule roleModule = module(MConst.Role);
        int gold = roleModule.getRoleRow().getGold();
        int money = roleModule.getRoleRow().getMoney();
        int bandGold = roleModule.getRoleRow().getBandGold();
        long tmpTime = System.currentTimeMillis();
        if (offlineTimestamp != 0 && offlineTimestamp < tmpTime) {
            tmpTime = offlineTimestamp;
        }
        long online = (tmpTime - loginTime) / 1000;
        if (online <= 0) {
            online = 1;
        }
        DungeonModule dungeon = module(MConst.Dungeon);
        int pg = dungeon.getRoleMaxDungeonId((byte) 0);
        int jg = dungeon.getRoleMaxDungeonId((byte) 1);
        //帮派id
        FamilyModule familyModule = module(MConst.Family);
        long familyId = familyModule.getAuth().getFamilyId();
        // imei
        String imei = loginInfo != null ? (loginInfo.getImei() != null ? loginInfo.getImei() : "") : "";
        String info = makeOfflineLog(online, gold, money, bandGold, pg, jg, familyId, imei);
    }


    private boolean isOtherRoleOnLine(String account, long roleid) {
        if (account == null) {
            return false;
        }
        AccountRow accountRow = MainStartup.accountMap.get(account);
        if (accountRow == null) {
            return false;
        }
        for (AccountRole role : accountRow.getRelativeRoleList()) {
            if (role.getRoleId().equals(roleid + "")) {
                continue;
            }
            if (PlayerSystem.system().getActors().get(role.getRoleId()) != null) {
                return true;
            }
        }

        return false;
    }

    private String makeOfflineLog(long online, int gold, int money, int bandgold, int pg, int jg, long familyAuthID, String imei) {
        StringBuilder sb = new StringBuilder();
        sb.append("online_time:").append(online).append("#")
                .append("main_coin:").append(gold).append("#")
                .append("gift_coin:").append(bandgold).append("#")
                .append("sub_coin:").append(money).append("#")
                .append("sp_case:").append(pg).append("#")
                .append("nm_case:").append(jg).append("#")
                .append("gang:").append(familyAuthID).append("#")
                .append("imei:").append(imei);
        return sb.toString();
    }

    public void logExit() {
        AccountRow accountRow = MainStartup.accountMap.get(this.account);

        Player player = PlayerSystem.get(id());
        if (player != null && player.isLogExit()) {
            return;
        }
        try {
            logRoleExit();
        } catch (Throwable t) {
            com.stars.util.LogUtil.error("踢玩家下线时打印离线日志出错, roleId=" + id(), t);
        }
        if (accountRow != null) {
            LoginInfo loginInfo = accountRow.getLoginInfo();
            if (loginInfo != null && loginInfo.getUid() != null) {
                long tmpTime = System.currentTimeMillis();
                if (offlineTimestamp != 0 && offlineTimestamp < tmpTime) {
                    tmpTime = offlineTimestamp;
                }
                long online = (tmpTime - loginTime) / 1000;
                if (online <= 0) {
                    online = 1;
                }
            }
            if (player != null) {
                player.setLogExit(true);
            }
        }
    }

    public void handleReconnect(ServerReconnect serverReconnect) {
        com.stars.util.LogUtil.info("重连|收到重连请求|roleId:{}|reconnectToken:{}", serverReconnect.getRoleId(), serverReconnect.getToken());
        if (serverReconnect == null) return;
        serverReconnect.getSession().putAttribute("reconnectToken", serverReconnect.getToken()); // 需要再次设置重连Token
        try {
            AccountRow accountRow = LoginModuleHelper.getOrLoadAccount(account, null);
            if (accountRow == null) {
                com.stars.util.LogUtil.info("重连|失败|存在账号|account:{}", account);
                reconnectFail(serverReconnect.getSession());
                return;
            }
            long curRoleId = serverReconnect.getRoleId();
            String token = serverReconnect.getToken();
            if (accountRow.getToken() == null || token == null) {
                com.stars.util.LogUtil.info("重连|失败|重连Token为空|accountRow.token:{}|session.token:{}",
                        accountRow.getToken(), token);
                reconnectFail(serverReconnect.getSession());
                return;
            }
            if (!accountRow.getToken().equals(token)) {
                com.stars.util.LogUtil.info("重连|失败|重连Token不相等|accountRow.token:{}|session.token:{}",
                        accountRow.getToken(), token);
                reconnectFail(serverReconnect.getSession());
                return;
            }
            //账号角色验证
            if (curRoleId != 0 && !isRoleIdValid(accountRow, curRoleId)) {
                com.stars.util.LogUtil.info("重连|失败|roleId非法|roleId:{}", curRoleId);
                reconnectFail(serverReconnect.getSession());
                return;
            }

            if (!self().canResend(serverReconnect.getMaxClientPacketId())) {
                com.stars.util.LogUtil.info("重连|失败|缓存包id失效|roleId:{}", curRoleId);
                reconnectFail(serverReconnect.getSession());
                return;
            }

            accountRow.setCurrentRoleId(curRoleId, accountRow.newRoleIdVersion()); // 强制设值
            if (state == ONLINE) {
                if (roleId != 0 && roleId != curRoleId) {
                    com.stars.util.LogUtil.info("重连|失败|roleId不相等|roleId:{}|curRoleId:{}", roleId, curRoleId);
                    reconnectFail(serverReconnect.getSession());
                } else {
                    com.stars.util.LogUtil.info("重连|成功|前状态:在线|roleId:{}|token:{}|connId:{}|session:{}|maxServerPacketId:{}",
                            serverReconnect.getRoleId(),
                            serverReconnect.getToken(),
                            serverReconnect.getSession().getConnectionId(),
                            serverReconnect.getSession().toString(),
                            self().getMaxServerPacketId());
                    setupSession(serverReconnect.getSession());
                    self().resend(serverReconnect.getMaxClientPacketId());
                    ClientReconnect packet = new ClientReconnect(true);
                    packet.setMaxServerPacketId(self().getMaxServerPacketId());
                    send(packet);

                    /* 关卡断线重连打印日志，好坑 */
                    try {
                        Module module = module(MConst.Dungeon);
                        module.onReconnect();
                    } catch (Throwable cause) {
                        com.stars.util.LogUtil.error("", cause);
                    }
                }
                return;
            }

            setupSession(serverReconnect.getSession());
            self().resend(serverReconnect.getMaxClientPacketId());
            fireReconnect();
            resetWhileLogin();
            ServiceHelper.summaryService().online(roleId);
            //重连打印登陆日志
            String time = accountRow.getRelativeRoleTime(curRoleId + "");
            accountRow.setLastLogLoginTime(System.currentTimeMillis());
            succeedReconnect();//重连成功，反馈客户端
            com.stars.util.LogUtil.info("重连|成功|前状态:离线|roleId:{}|token:{}|connId:{}|session:{}|maxServerPacketId:{}",
                    serverReconnect.getRoleId(),
                    serverReconnect.getToken(),
                    serverReconnect.getSession().getConnectionId(),
                    serverReconnect.getSession().toString(),
                    self().getMaxServerPacketId());
        } catch (Throwable t) {
            t.printStackTrace();
            com.stars.util.LogUtil.error("重连|出错|account:" + account + "|roleId:" + serverReconnect.getRoleId(), t);
        }
    }

    private void reconnectFail(GameSession session) {
        PacketManager.send(session, new ClientReconnect(false));
    }

    private boolean isRoleIdValid(AccountRow accountRow, long roleId) {
        List<AccountRole> accountRoleList = accountRow.getRelativeRoleList();
        String roleIdStr = Long.toString(roleId);
        for (int i = 0, len = accountRoleList.size(); i < len; i++) {
            if (accountRoleList.get(i).getRoleId().equals(roleIdStr)) {
                return true;
            }
        }
        return false;
    }

    private void squeezeAndSetCurrentRoleId(long roleId, int roleIdVersion) {
        try {
            AccountRow accountRow = LoginModuleHelper.getOrLoadAccount(account, null);
            accountRow.getLoginLock().lock();
            try {
                // 先记下账号下的其他角色
                long prevRoleId = accountRow.getCurrentRoleId();
                if (accountRow.setCurrentRoleId(roleId, roleIdVersion)) {
                    SessionManager.put(roleId, session);
                    if (prevRoleId != 0 && prevRoleId != roleId) { // 挤其他人，自己不挤，因为异步消息
                        Player player = PlayerSystem.get(prevRoleId);
                        if (player != null) {
                            try {
                                player.tell(new SqueezeMsg(), null);
                            } catch (Throwable t) {
                                com.stars.util.LogUtil.info("设置AccountRow.roleId, 挤号失败, roleId={}, prevRoleId={}", roleId, prevRoleId);
                            }
                        }
                    }
                    com.stars.util.LogUtil.info("设置AccountRow.roleId成功, roleId={}, roleIdVersion={}", roleId, roleIdVersion);
                } else {
                    com.stars.util.LogUtil.info("设置AccountRow.roleId失败, roleId={}, roleIdVersion={}", roleId, roleIdVersion);
                }
            } finally {
                accountRow.getLoginLock().unlock();
            }
        } catch (SQLException e) {
            com.stars.util.LogUtil.error("", e);
        }
    }

    private void setCurrentRoleIdToZero() {
        try {
            AccountRow accountRow = LoginModuleHelper.getOrLoadAccount(account, null);
            accountRow.getLoginLock().lock();
            try {
                if (accountRow.compareAndSetCurrentRoleId(roleId, 0)) {
                    com.stars.util.LogUtil.info("设置AccountRow.roleId为0成功, 原roleId=" + roleId);
                } else {
                    com.stars.util.LogUtil.info("设置AccountRow.roleId为0失败, 原roleId=" + roleId);
                }
            } finally {
                accountRow.getLoginLock().unlock();
            }
        } catch (SQLException e) {
            LogUtil.error("", e);
        }
    }

    public boolean isOnline() {
        return this.state == ONLINE;
    }

    public int getChannnel() {
        AccountRow accountRow = null;
        int channnel = 0;
        try {
            accountRow = LoginModuleHelper.getOrLoadAccount(getAccount(), null);
            LoginInfo loginInfo = accountRow.getLoginInfo();
            String channelStr = "";
            if (loginInfo == null && (snapChannelId != null && !snapChannelId.equals(""))) {
                channelStr = snapChannelId.split("@")[0];
            } else {
                channelStr = loginInfo.getChannel().split("@")[0];
            }
            channnel = Integer.parseInt(channelStr);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return channnel;
    }
}
