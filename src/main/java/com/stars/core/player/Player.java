package com.stars.core.player;

import com.stars.core.actor.AbstractActor;
import com.stars.core.actor.Actor;
import com.stars.core.actor.DeadMessageHandler;
import com.stars.core.event.EventDispatcher;
import com.stars.core.exception.LogicException;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Guard;
import com.stars.core.module.Module;
import com.stars.core.module.ModuleContext;
import com.stars.core.redpoint.RedPoints;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.network.PacketChecker;
import com.stars.network.PacketUtil;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.network.server.session.GameSession;
import com.stars.services.ServiceHelper;
import com.stars.services.summary.SummaryComponent;
import com.stars.util.I18n;
import com.stars.util.LogUtil;
import com.stars.util.ServerLogConst;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by zws on 2015/11/30.
 */
public class Player extends AbstractActor {

    public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private long id;
    private volatile GameSession session; // 会话
    private EventDispatcher eventDispatcher; // 事件系统
    private RedPoints redPoints; // 红点
    private ModuleContext context; // 上下文
    private Map<String, Module> moduleMap; // 保证迭代是有序的
    private Guard guard; // 用来模块内的访问控制（主要用于登录过程，因为登录过程是异步，要防止客户端乱发包）
    private Queue<Packet> lazyQueue = new LinkedBlockingQueue<>();
    private String userName = "";
    private long loginTime;
    private byte from = 0;//从内存或者db获得,0内存,1db

    /* 断线重连相关，防丢包 */
    private int packetId; // 包序号
    private Object sendLock = new Object();
    private Deque<Packet> sendDeque = new LinkedList<>();
    private int maxServerPacketId;
    private boolean logExit = false;//是否记录过登出日志

    // fixme: 暂定的构造方法
    public Player(long id) {
        this.id = id;
    }

    public void init(Map<String, Module> moduleMap, EventDispatcher eventDispatcher, ModuleContext context, RedPoints redPoints) {
        this.moduleMap = moduleMap;
        this.eventDispatcher = eventDispatcher;
        this.context = context;
        this.redPoints = redPoints;
        /* 注入content和redpoint */
        inject();
        /*
         * 获取第一个模块用作访问控制（因为moduleMap是LinkedHashMap，所以迭代是有顺序的）
         */
        for (Module m : moduleMap.values()) {

            if (m instanceof Guard) {
                guard = (Guard) m;
                break;
            }
        }

        setDeadMessageHandler(new DeadMessageHandler() {
            @Override
            public void handle(Object message, Actor sender) {
                LogUtil.debug("dead letter, class: " + message.getClass().getSimpleName() + ", object: " + message);
            }
        });
    }

    @Override
    public void onReceived(Object obj, Actor actor) {
        if (obj instanceof Packet) {
            Packet packet = (Packet) obj;
            if (PacketChecker.needCache(packet.getType())
                    && packet.getPacketId() > maxServerPacketId) { // 记录客户端上传的packetId(默认大于0)
                maxServerPacketId = packet.getPacketId();
//                LogUtil.info("收包|maxServerPacketId:{}|serverPacketId:{}", maxServerPacketId, packet.getPacketId());
            }
            if (packet.getType() != 0 && packet.getType() != 47 && ServerLogConst.logDebug) {
//                LogUtil.info("收包|packetType:{}|serverPacketId:{}",
//                        String.format("0x%04X", packet.getType()), packet.getPacketId());
            }
            if (guard.canAccess(packet)) {
                try {
                    if (packet instanceof PlayerPacket) {
                        ((PlayerPacket) packet).setPlayer(this);
                        ((PlayerPacket) packet).execPacket(this);
                    } else {
                        packet.execPacket();
                    }
                    calculateAndFlushRedPoint(); // 计算并下发红点数据
                    updateSummaryComponent(); // 更新摘要数据
                } catch (LogicException e) {
                    String message = I18n.get(e.getMessage(), (Object[]) e.getParams());
                    if (message.equals("国际化请求失败")) {
                        message = e.getMessage();
                        PacketManager.send(packet.getSession(), new ClientText(message, e.getParams()));
                    } else {
                        PacketManager.send(packet.getSession(), new ClientText(message, e.getParams()));
                    }
                } catch (Exception e) {
                    LogUtil.error(e.getMessage(), e);
                    PacketManager.send(packet.getSession(), new ClientText("请求异常"));
                } finally {
                    guard.onCallAccess(packet.getPacketId());
                }
            }
        }
    }

    public long id() {
        return this.id;
    }

    Map<String, Module> moduleMap() {
        return moduleMap;
    }

    EventDispatcher eventDispatcher() {
        return eventDispatcher;
    }

    public void session(GameSession session) {
        this.session = session;
    }

    public GameSession session() {
        return session;
    }

    public void send(Packet packet) {
        if (PacketChecker.needCache(packet.getType())) { // 要过滤一些特殊的包
            packet.setBytes(PacketUtil.packetToBytes(packet));
            synchronized (sendLock) {
                packet.setPacketId(++packetId); // 设置包序号
                if (sendDeque.size() >= 24) {
                    sendDeque.poll(); // 丢弃
                }
                sendDeque.offer(packet);
            }
            PacketManager.send(id, packet.getType(), packet.getPacketId(), packet.getBytes()); // 防止再次序列化
        } else {
            PacketManager.send(session, packet);
        }
//        LogUtil.info("发包|roleId:{}|packetType:{}|packetId:{}",
//                id, String.format("0x%04X", packet.getType()), packet.getPacketId());
    }

    /* 重连相关 */
    public boolean canResend(int maxClientPacketId) {
        synchronized (sendLock) {
            Packet firstPacket = sendDeque.getFirst();
            Packet lastPacket = sendDeque.getLast();
            // 日志
//            LogUtil.info("重新发包|判断|firstPacketId:{}|lastPacketId:{}|maxClientPacketId:{}",
//                    firstPacket != null ? firstPacket.getPacketId() : "null",
//                    lastPacket != null ? lastPacket.getPacketId() : "null",
//                    maxClientPacketId);
            // 判断能否重发数据
            if ((firstPacket != null && firstPacket.getPacketId() > maxClientPacketId)
                    || (lastPacket != null && lastPacket.getPacketId() < maxClientPacketId)) {
                return false;
            }
            return true;
        }
    }

    public void resend(int maxClientPacketId) {
        synchronized (sendLock) {
            Packet firstPacket = sendDeque.getFirst();
            Packet lastPacket = sendDeque.getLast();
            if (firstPacket != null && firstPacket.getPacketId() > maxClientPacketId) {
//                LogUtil.info("重新发包|失败|roleId:{}|maxClientPacketId:{}|firstPacketId:{}|lastPacketId:{}",
//                        id, maxClientPacketId, firstPacket.getPacketId(), lastPacket.getPacketId());
                return;
            }
            for (Packet packet : sendDeque) {
                if (packet.getPacketId() > maxClientPacketId) {
                    PacketManager.send(id, packet.getType(), packet.getPacketId(), packet.getBytes());
//                    LogUtil.info("重新发包|roleId:{}|packetType:{}|packetId:{}",
//                            id, String.format("0x%04X", packet.getType()), packet.getPacketId());
                }
            }
        }
    }

    public int getMaxServerPacketId() {
        return maxServerPacketId;
    }

    public void setMaxServerPacketIdToZero() {
        this.maxServerPacketId = 0;
    }

    public void resetResendData() {
        synchronized (sendLock) {
            packetId = 0;
            sendDeque.clear();
        }
        maxServerPacketId = 0;
    }

    /**
     * 延迟发包
     *
     * @param packet
     */
    public void lazySend(Packet packet) {
        lazyQueue.add(packet);
    }

    public Queue<Packet> getLazyQueue() {
        return lazyQueue;
    }

    public void clearLazyQueue() {
        lazyQueue.clear();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void inject() {
        try {
            Field contextField = AbstractModule.class.getDeclaredField("context");
            Field redPointsField = AbstractModule.class.getDeclaredField("redPoints");
            for (Module module : moduleMap().values()) {
                // 注入上下文
                contextField.setAccessible(true);
                contextField.set(module, context);
                contextField.setAccessible(false);
                // 注入红点系统
                redPointsField.setAccessible(true);
                redPointsField.set(module, redPoints);
                redPointsField.setAccessible(false);
            }
        } catch (Throwable t) {
            LogUtil.error("", t);
        }
    }

    public RedPoints getRedPoints() {
        return redPoints;
    }

    private void calculateAndFlushRedPoint() {
        try {
            Map<Integer, String> redPointMap = new HashMap<>();
            Map<String, Set<Integer>> signCalMap = redPoints.getSignCalRedPointMap();
            if (signCalMap.isEmpty())
                return;
            for (Map.Entry<String, Set<Integer>> entry : signCalMap.entrySet()) {
                AbstractModule module = (AbstractModule) moduleMap.get(entry.getKey());
                List<Integer> list = new LinkedList<>();
                list.addAll(entry.getValue());
                try {
                    module.calRedPoint(list, redPointMap);
                } catch (Exception e) {
                    LogUtil.error("计算红点异常, moduleName=" + entry.getKey(), e);
                }
            }
            redPoints.setChangeMap(redPointMap);
        } catch (Throwable t) {
            LogUtil.error("计算红点异常", t);
        } finally {
            redPoints.flush();
        }
    }

    private void updateSummaryComponent() {
        try {
            Map<String, SummaryComponent> compMap = new HashMap<>();
            for (String moduleName : context.getSummaryComponentUpdateMarkSet()) {
                try {
                    Module module = moduleMap.get(moduleName);
                    if (module != null) {
                        module.onUpateSummary(compMap);
                    } else {
                        LogUtil.error("更新摘要数据|模块:{}|异常:模块为空", moduleName);
                    }
                } catch (Throwable t) {
                    LogUtil.error("更新摘要数据|模块:" + moduleName + "|异常:" + t.getMessage(), t);
                }
            }
            ServiceHelper.summaryService().updateSummaryComponent(id, compMap);
        } catch (Throwable t) {
            LogUtil.error("更新摘要数据|异常:" + t.getMessage(), t);
        } finally {
            context.getSummaryComponentUpdateMarkSet().clear();
        }
    }

    public long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }

    public String getAccountByRoleId() {
        LoginModule loginModule = (LoginModule) moduleMap.get(MConst.Login);
        return loginModule.getAccount();
    }

    public byte getFrom() {
        return from;
    }

    public void setFrom(byte from) {
        this.from = from;
    }

    public boolean isLogExit() {
        return logExit;
    }

    public void setLogExit(boolean logExit) {
        this.logExit = logExit;
    }
}
