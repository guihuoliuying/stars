package com.stars.services.pvp;

import com.google.gson.Gson;
import com.stars.bootstrap.ServerManager;
import com.stars.core.player.PlayerUtil;
import com.stars.ExcutorKey;
import com.stars.core.schedule.SchedulerManager;
import com.stars.modules.pk.PKManager;
import com.stars.modules.pk.event.EnterPkEvent;
import com.stars.modules.pk.event.FinishPkEvent;
import com.stars.modules.pk.packet.ModifyConnectorRoute;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFight;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterPK;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.ServerConnSessionManager;
import com.stars.multiserver.fight.FightIdCreator;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.fightServerManager.FSManagerServiceActor;
import com.stars.services.pvp.cache.PvpCache;
import com.stars.services.role.RoleNotification;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;
import com.stars.core.actor.invocation.ServiceActor;
import io.netty.buffer.Unpooled;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PVPServiceActor extends ServiceActor implements PVPService {

    //    private SocketClient sc;
    private Map<Long, byte[]> dataMap;//临时存放每场PK的初始数据，以邀请者ID做key
    private Map<Long, PvpCache> pvpCacheMap;// pk数据缓存,以邀请者ID做key

    private int connId;
    private Gson gson;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd("pkService", this);
        connId = ServerManager.getServer().getConfig().getServerId() * 100 + ServerConnSessionManager.CONN_ID_PVP_M2F;
//		initSocketClient();
        dataMap = new HashMap<>();
        pvpCacheMap = new ConcurrentHashMap<>();
        gson = new Gson();
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.PVP, new TimingCheckTask(), 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void printState() {
        LogUtil.info("容器大小输出:{},dataMap:{},pvpCacheMap:{}", this.getClass().getSimpleName(), dataMap.size(), pvpCacheMap.size());
    }

    //    private void initSocketClient() {
//        Properties p = ServerManager.getServer().getConfig().getProps().get(BootstrapConfig.FIGHT);
//        sc = new SocketClient(p.getProperty("serverIp"), Integer.parseInt(p.getProperty("serverPort")),
//                MultiServerHelper.SOCKETCLIENT_FLAG_PVPSERVICE2FIGHTSERVER,
//                connId);
////		if (sc != null && sc.isConnect()) {
////			int serverId = ServerManager.getServer().getConfig().getServerId();
//        sc.send(new RegistConnToFightServer(connId));
////		}
//    }

    @Override
    public void startPVP(long invitor, long invitee, byte[] data) {
//        if (sc == null || !sc.isConnect()) {
//            initSocketClient();
//        }
//        dataMap.put(invitor, data);
//        StartPVP1FightPacket sp = new StartPVP1FightPacket();
//        sp.setInitData(data);
//        sp.setInvitor(invitor);
//        sp.setInvitee(invitee);
//        sp.setServerId(connId);
//        sc.send(sp);
    }

    @Override
    public void startPvp(long inviterId, long inviteeId, ClientEnterFight enterPacket) {
        dataMap.put(inviterId, packetToBytes(enterPacket));

        MainRpcHelper.fightBaseService().createFight(
                ServiceHelper.fsManagerService().getFightServer(FSManagerServiceActor.FIGHT_SERVER_LEVEL_COMM),
                FightConst.T_PVP,
                MultiServerHelper.getServerId(),
                "pvp" + FightIdCreator.creatUUId(),
                enterPacket,
                new PvpFightInitData(inviterId, inviteeId));
    }


    @Override
    public void socketDisconnect() {
//        sc = null;
    }

    @Override
    public void onFightCreationSucceeded(int mainServerId, int fightServerId, String fightId, long inviterId, long inviteeId) {
        // 切路由
        MultiServerHelper.modifyConnectorRoute(inviterId, fightServerId);
        MultiServerHelper.modifyConnectorRoute(inviteeId, fightServerId);

        noticeClientEnterPK(inviterId, inviteeId);
        newPvpCache(fightServerId, inviterId, inviteeId, dataMap.get(inviterId), fightId);
        dataMap.remove(inviterId);
    }

    @Override
    public void finishPvp(int mainServerId, long inviterId, String fightId, long winnerId, long loserId) {
        // 移除pvp缓存数据
        PvpCache pvpCache = pvpCacheMap.remove(inviterId);
        pvpCache.setStatus(SceneManager.STAGE_VICTORY);
        // 更改路由
        MultiServerHelper.modifyConnectorRoute(winnerId, mainServerId);
        MultiServerHelper.modifyConnectorRoute(loserId, mainServerId);
        // 通知module pvp结束
        ServiceHelper.roleService().notice(winnerId, new FinishPkEvent(SceneManager.STAGE_VICTORY, winnerId));
        ServiceHelper.roleService().notice(loserId, new FinishPkEvent(SceneManager.STAGE_FAIL, loserId));
    }

    @Override
    public void handleDamage(int mainServerId, long inviterId, Map<String, HashMap<String, Integer>> damageMap) {
        dealDamage(inviterId, damageMap);
    }

    @Override
    public void handleDead(int mainServerId, long inviterId, Map<String, String> deadMap) {
        dealDead(inviterId, deadMap);
    }

    /**
     * 通知客户端进入PK
     *
     * @param invitor 邀请者
     * @param invitee 被邀请者
     */
    public void noticeClientEnterPK(long invitor, long invitee) {
        byte[] initData = dataMap.get(invitor);
        EnterPkEvent e = new EnterPkEvent();
        e.setInvitor(invitor);
        e.setInvitee(invitee);
        e.setData(initData);
        ServiceHelper.roleService().notice(invitor, new RoleNotification(e));
        ServiceHelper.roleService().notice(invitee, new RoleNotification(e));
    }

    @Override
    public void onReceived0(Object message, Actor sender) {
//        if (message instanceof PVP1FightBackPacket) {
//            PVP1FightBackPacket pbp = (PVP1FightBackPacket) message;
//            //通知连接服更改路由
//            ModifyConnectorRoute mcr = new ModifyConnectorRoute();
//            String serverId = ServerManager.getServer().getConfig().getProps().get(BootstrapConfig.FIGHT).getProperty("serverId");
//            mcr.setServerId(Integer.parseInt(serverId));
//            mcr.setRoleId(pbp.getInviter());
//            PacketManager.send(pbp.getInviter(), mcr);
//            mcr.setRoleId(pbp.getInvitee());
//            PacketManager.send(pbp.getInvitee(), mcr);
//            noticeClientEnterPK(pbp.getInviter(), pbp.getInvitee());
//            newPvpCache(pbp.getInviter(), pbp.getInvitee(), dataMap.get(pbp.getInviter()), pbp.getActorId());
//            dataMap.remove(pbp.getInviter());
//        } else if (message instanceof PVPResultPacket) {
//            // 战斗服返回结束结果,只有退出/离线情况才会走这里
//            PVPResultPacket resultPacket = (PVPResultPacket) message;
//            long invitor = resultPacket.getInvitor();
//            long winner = resultPacket.getVictor();
//            long loser = resultPacket.getLoser();
//            // 移除pvp缓存数据
//            PvpCache pvpCache = pvpCacheMap.get(invitor);
//            pvpCache.setStatus(SceneManager.STAGE_VICTORY);
//            pvpCacheMap.remove(invitor);
//            // 更改路由
//            ModifyConnectorRoute mcr = new ModifyConnectorRoute();
//            mcr.setServerId(ServerManager.getServer().getConfig().getServerId());
//            mcr.setRoleId(winner);
//            PacketManager.send(winner, mcr);
//            mcr.setRoleId(loser);
//            PacketManager.send(loser, mcr);
//            // 通知module pvp结束
//            ServiceHelper.roleService().notice(winner, new FinishPkEvent(SceneManager.STAGE_VICTORY, loser));
//            ServiceHelper.roleService().notice(loser, new FinishPkEvent(SceneManager.STAGE_FAIL, winner));
//        } else if (message instanceof LuaFrameDataBack) {
//            LuaFrameDataBack luaFrameDataBack = (LuaFrameDataBack) message;
//            LuaFrameData frameData = gson.fromJson(luaFrameDataBack.getjData(), LuaFrameData.class);
//            long invitorId = Long.parseLong(luaFrameDataBack.getKey());
//            if (frameData.getDead() != null && !frameData.getDead().isEmpty()) {
//                dealDead(invitorId, frameData.getDead());
//            }
//            if (frameData.getDamage() != null && !frameData.getDamage().isEmpty()) {
//                dealDamage(invitorId, frameData.getDamage());
//            }
//        }
    }

//    private void newPvpCache(long invitor, long invitee, byte[] data, int fightActorId) {
//        NewByteBuffer buffer = new NewByteBuffer(Unpooled.buffer());
//        buffer.getBuff().writeBytes(data);
//        ClientEnterPK enterPK = new ClientEnterPK();
//        enterPK.readFromBuffer(buffer);
//        buffer.getBuff().release();
//        PvpCache pvpCache = new PvpCache(invitor, invitee, enterPK.getFighterEntityList(), fightActorId);
//        pvpCacheMap.put(invitor, pvpCache);
//    }

    private void newPvpCache(int fightServer, long invitor, long invitee, byte[] data, String fightId) {
        NewByteBuffer buffer = new NewByteBuffer(Unpooled.buffer());
        buffer.getBuff().writeBytes(data);
        ClientEnterPK enterPK = new ClientEnterPK();
        enterPK.readFromBuffer(buffer);
        buffer.getBuff().release();
        PvpCache pvpCache = new PvpCache(invitor, invitee, enterPK.getFighterEntityList(), fightId);
        pvpCache.setFightServer(fightServer);
        pvpCacheMap.put(invitor, pvpCache);
    }

    private void dealDead(long invitorId, Map<String, String> deadMap) {
        PvpCache pvpCache = pvpCacheMap.get(invitorId);
        long loser;
        long winner;
        for (String deadId : deadMap.keySet()) {
            if (pvpCache.isPlayer(deadId)) {
                loser = Long.valueOf(deadId);
                winner = loser == invitorId ? pvpCache.getInvitee() : invitorId;
                pvpFinish(invitorId, winner, loser);
            }
        }
    }

    private void dealDamage(long invitorId, Map<String, HashMap<String, Integer>> damageMap) {
        PvpCache pvpCache = pvpCacheMap.get(invitorId);
        for (Map.Entry<String, HashMap<String, Integer>> entry : damageMap.entrySet()) {
            for (int value : entry.getValue().values()) {
                pvpCache.updateHp(entry.getKey(), value);
            }
        }
    }

    /**
     * pvp结束
     *
     * @param invitor
     * @param winner
     * @param loser
     */
    private void pvpFinish(long invitor, long winner, long loser) {
        PvpCache pvpCache = pvpCacheMap.get(invitor);
        // 停止战斗服actor
//        sc.send(new StopFightActor(pvpCache.getFightActorId()));
        // fixme:
        MainRpcHelper.fightBaseService().stopFight(
                pvpCache.getFightServer(),
                FightConst.T_PVP,
                MultiServerHelper.getServerId(),
                pvpCache.getFightId());
        LogUtil.info("战斗结束,winner={},loser={}，通知到游戏服", winner, loser);
        // 移除pvp缓存数据
        pvpCache.setStatus(SceneManager.STAGE_VICTORY);
        pvpCacheMap.remove(invitor);
        // 更改路由
        ModifyConnectorRoute mcr = new ModifyConnectorRoute();
        mcr.setServerId(ServerManager.getServer().getConfig().getServerId());
        mcr.setRoleId(winner);
        PlayerUtil.send(winner, mcr);
        mcr.setRoleId(loser);
        PlayerUtil.send(loser, mcr);
        // 通知module pvp结束
        ServiceHelper.roleService().notice(winner, new FinishPkEvent(SceneManager.STAGE_VICTORY, loser));
        ServiceHelper.roleService().notice(loser, new FinishPkEvent(SceneManager.STAGE_FAIL, winner));
    }

    private byte[] packetToBytes(Packet packet) {
        NewByteBuffer buffer = new NewByteBuffer(Unpooled.buffer());
        packet.writeToBuffer(buffer);
        byte[] bytes = new byte[buffer.getBuff().readableBytes()];
        buffer.getBuff().readBytes(bytes);
        buffer.getBuff().release();
        return bytes;
    }

    class TimingCheckTask implements Runnable {

        @Override
        public void run() {
            if (pvpCacheMap.isEmpty())
                return;
            for (PvpCache pvpCache : pvpCacheMap.values()) {
                if (pvpCache.getStatus() != SceneManager.STAGE_PROCEEDING)
                    continue;
                if (System.currentTimeMillis() - pvpCache.getStartTimestamp() < PKManager.pvpLimitTime)
                    continue;
                try {
                    long[] result = pvpCache.judgeVictory();
                    pvpFinish(pvpCache.getInvitor(), result[0], result[1]);
                } catch (Exception e) {
                    LogUtil.error("", e);
                    continue;
                }
            }
        }
    }
}
