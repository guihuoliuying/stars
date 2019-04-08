package com.stars.multiserver.LootTreasure;

import com.google.gson.Gson;
import com.stars.bootstrap.ServerManager;
import com.stars.modules.MConst;
import com.stars.modules.drop.DropManager;
import com.stars.modules.loottreasure.LootTreasureConstant;
import com.stars.modules.loottreasure.LootTreasureManager;
import com.stars.modules.loottreasure.packet.*;
import com.stars.modules.loottreasure.prodata.LootSectionVo;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientRoleRevive;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.packet.ServerExitFight;
import com.stars.modules.scene.packet.ServerRoleRevive;
import com.stars.multiserver.LootTreasure.event.AddFighterBackEvent;
import com.stars.multiserver.LootTreasure.event.CreateFightBackEvent;
import com.stars.multiserver.LootTreasure.event.FightFrameEvent;
import com.stars.multiserver.LootTreasure.event.LTOfflineEvent;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.fight.RoleId2ActorIdManager;
import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.multiserver.packet.ClientClearOtherFighters;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.network.server.session.SessionManager;
import com.stars.server.connector.packet.FrontendClosedN2mPacket;
import com.stars.server.fight.MultiServer;
import com.stars.server.main.actor.ActorServer;
import com.stars.server.main.message.Disconnected;
import com.stars.services.ServiceHelper;
import com.stars.services.fightServerManager.FSManagerServiceActor;
import com.stars.startup.LootTreasureStartup;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class PVPLootTreasure extends AbstractLootTreasure {

    //注意：这里的标识0,1不要改动，因为是和commondefine里的loot_boxlabelcount下表对应的;
    public static byte ROOM_FLAG_LOW = 0;//低级房标志
    public static byte ROOM_FLAG_HIGH = 1;//高级房标志

    public static long WAIT_STEP_TIME = 5000l;//等待阶段时长

    public static long FIGHT_STEP_TIME = 3600000l;//战斗持续时长

    public static long DEAD_MATCH_TIME = 5000L;//死亡后下一次匹配时间

    public static long IDLE_ROOM_RECYCEL_TIME = 15000L;//房间只剩下一个人后等待匹配的时间，超时则回收

    public static long CREATE_FACTOR_DELAY = 8000L;//创建战斗actor的超时时间

    public static long ROOM_ACTIVITY_TIME = 20000L;//房间的不活跃时间

    /**
     * 高级房
     */
    private LinkedList<LTRoom> rooms_high;

    /**
     * 低级房
     */
    private LinkedList<LTRoom> rooms_low;

    private Map<Integer, LTRoom> rooms_all;

    /**
     * 等待匹配
     */
    private LinkedList<Looter> waitMatch;

    private PVPLootTreasureRunner runner;

    private int roomIdCounter;


    private Gson gson;

    //伤害排行榜客户端需求显示的条数;
    public final static int RANK_CLIENT_SHOW_COUNT = 5;

    private int serverId;


    private LootTreasureStartup lootTreasureStartup;
    private int preSyncRankToMainTime = 0;
    private int gapSyncRankToMain = 1;
    private LootSectionVo lootSectionVo;
    //备份离线的looter map; 用于离线并且被踢了的玩家在获取不到looters时，从这里再拿去数据;
    public Map<Long, Looter> bakOfflineLooterMap = new ConcurrentHashMap<>();

    public PVPLootTreasure(LTActor ltActor) {
        super(ltActor);
        lootTreasureStartup = (LootTreasureStartup) ((MultiServer) ServerManager.getServer()).getBusiness();
        ltDamageRank = new LTDamageRank();
        roomIdCounter = 0;
        rooms_all = new HashMap<Integer, LTRoom>();
        rooms_high = new LinkedList<LTRoom>();
        rooms_low = new LinkedList<LTRoom>();
        waitMatch = new LinkedList<Looter>();
        serverId = ServerManager.getServer().getConfig().getServerId();
        gson = new Gson();
        lootSectionVo = LootTreasureManager.getLootSectionVo(Integer.parseInt(ltActor.getId()));

    }

    private void removeLooterFromFightServer(Looter looter) {
        if (looter != null && looter.getRoom() != null) {
            List<Long> removeFighterIdList = new ArrayList<>();
            removeFighterIdList.add(looter.getId());
            //将玩家移除战斗服;
            int fightServerId = looter.getRoom().getFightServer();
            short handlerType = FightConst.T_LOOT_TREASURE;
            int fromServerId = MultiServerHelper.getServerId();
            String fightId = looter.getRoom().getFightActor();
            RMLTRPCHelper.fightBaseService().removeFromFightActor(fightServerId,
                    handlerType, fromServerId, fightId, removeFighterIdList);
        }
    }

    @Override
    void startRunner() {
        step = STEP_WAIT;
        nextStepTime = System.currentTimeMillis() + LootTreasureManager.PVP_WAIT_TIME;
        runner = new PVPLootTreasureRunner();
        runner.runnable = true;
        runner.start();
    }

    @Override
    public void onReceived(Object message) {
        if (message instanceof RunEvent) {
            RunEvent rEvent = (RunEvent) message;
            if (rEvent.geteId() != this.runner.getrEvent().geteId()) {
                return;
            }
            doRun();
            return;
        }
        if (message instanceof AttendLootTreasure) {
            //游戏服玩家发起参加夺宝活动
            AttendLootTreasure attendLootTreasure = (AttendLootTreasure) message;
            RMLTRPCHelper.lootTreasureService().setRoleAtLootSection(attendLootTreasure.getRoleId(), ltActor.getId());
            newLooterCome(attendLootTreasure.getServerId(), attendLootTreasure.getServerName(), attendLootTreasure.getfEntity(), attendLootTreasure.getJobId());
            return;
        }
        //创建新房间返回;
        if (message instanceof CreateFightBackEvent) {
            //战斗服创建战斗成功返回
            CreateFightBackEvent createFightActorBack = (CreateFightBackEvent) message;
            LTRoom ltRoom = rooms_all.get(createFightActorBack.getRoom());
            if (ltRoom == null) {
                //房间已被回收了，无效的战斗actor，回收吧
//            	RMLTRPCHelper.fightBaseService().stopFight(fightServerId, FightConst.T_LOOT_TREASURE, serverId, createFightActorBack.getFightActor());
                return;
            }
            ltRoom.setCreateActorTimeDelay(0);
            ltRoom.setFightActor(createFightActorBack.getFightActor());
            LootTreasureManager.log("夺宝服 创建新房间返回" + "crateactor back actorid=" + createFightActorBack.getFightActor() +
                    ",roomid=" + ltRoom.getId());
            ltRoom.setLastActivityTime(System.currentTimeMillis());
            return;
        }
        //有新玩家匹配到新房间返回;
        if (message instanceof AddFighterBackEvent) {
            LootTreasureManager.log("夺宝服 有新玩家匹配到新房间返回");
            AddFighterBackEvent aBackEvent = (AddFighterBackEvent) message;

            LTRoom ltRoom = rooms_all.get(aBackEvent.getRoom());

            Set<Long> set = aBackEvent.getFighters();

            ClientLootTreasureInfo clientLootTreasureInfo = new ClientLootTreasureInfo(ClientLootTreasureInfo.TYPE_ENTRY_PVP);

            for (long long1 : set) {
                if (offlineLooters.contains(long1)) {
                    continue;
                }

                MultiServerHelper.modifyConnectorRoute(long1, ltRoom.getFightServer());

                Looter looter = ltRoom.getLooter(long1);
                //同步杀人排行榜;
                rankSort(true, looter.getId(), false);
                //同步当前人员的宝箱数量;
                syncToClientBoxCount(ltRoom);
                LootTreasureManager.log("夺宝服 玩家" + long1 + "匹配成功，房间id=" + ltRoom.getId());

                clientLootTreasureInfo.setRoomType(looter.getRoom_flag());
                clientLootTreasureInfo.setSwitchRoomEndCdStamp(looter.getLastManualSwitchRoomEndStamp());
                sendToClient(long1, clientLootTreasureInfo);
            }
            return;
        }
        if (message instanceof FightFrameEvent) {
            FightFrameEvent luaFrameDataBack = (FightFrameEvent) message;
            LTRoom ltRoom = rooms_all.get(luaFrameDataBack.getRoom());
            if (ltRoom != null) {
                ltRoom.setLastActivityTime(System.currentTimeMillis());
            }
            this.doLuaFramData(luaFrameDataBack.getlFrameData());
            return;
        }
        if (message instanceof LTOfflineEvent) {
            LTOfflineEvent offlineNotice = (LTOfflineEvent) message;
            if (looters.containsKey(offlineNotice.getRoleId())) {
                offlineLooters.add(offlineNotice.getRoleId());
                Looter looter = looters.get(offlineNotice.getRoleId());
                removeLooterFromFightServer(looter);
            }
            return;
        }
        if (message instanceof ServerExitFight) {
            ServerExitFight serverExitFight = (ServerExitFight) message;
            Looter looter = looters.get(serverExitFight.getRoleId());
            //发送离开战斗协议到主服中;
            if (looter != null) {
                removeLooterFromFightServer(looter);
                offlineLooters.add(serverExitFight.getRoleId());
                LootTreasureManager.log("夺宝服PVP 请求离开战斗");
                //因为是发到主服的,所以不需要KEY;
                RMLTRPCHelper.lootTreasureService().existFight(looter.getServerId(), serverExitFight.getRoleId());
            }
            return;
        }
        if (message instanceof FrontendClosedN2mPacket || message instanceof Disconnected) {
            Packet p = (Packet) message;
            if (looters.containsKey(p.getRoleId())) {
                offlineLooters.add(p.getRoleId());
                removeLooterFromFightServer(looters.get(p.getRoleId()));
            }
            return;
        }
        if (message instanceof ServerRoleRevive) {
            ServerRoleRevive serverRoleRevive = (ServerRoleRevive) message;
            Looter looter = this.looters.get(serverRoleRevive.getRoleId());
            if (looter != null) {
                revive(serverRoleRevive.getRoleId());
            }
            return;
        }
        if (message instanceof ServerRequestSwitchRoom) {
            ServerRequestSwitchRoom serverRequestSwitchRoom = (ServerRequestSwitchRoom) message;
            Looter looter = this.looters.get(serverRequestSwitchRoom.getRoleId());
            if (looter != null) {
                byte rtnRoomState = looter.requestSwitchRoom(serverRequestSwitchRoom.getRoomType());
                int diffCount = 0;
                if (rtnRoomState < 0) {
                    diffCount = looter.getDiffBoxCountByRoomType(serverRequestSwitchRoom.getRoomType());
                }
                syncLooterSwitchedRoom(looter, rtnRoomState, diffCount, false);
            }
            return;
        }
    }

    @Override
    public void newLooterCome(int serverId, String serverName, FighterEntity fEntity, int jobId) {
        fEntity.fighterType = FighterEntity.TYPE_PLAYER;
        long id = Long.parseLong(fEntity.getUniqueId());
        if (step == STEP_FIGHT) {
            PacketManager.send(Long.parseLong(fEntity.getUniqueId()), LTActor.newClientEnterPK(fEntity));
            //同步杀人排行榜;
            rankSort(true, Long.parseLong(fEntity.getUniqueId()), false);
        }
        if (!this.looters.containsKey(id)) {
            Looter looter = new Looter(serverId, serverName, fEntity, jobId);
            //去拿下备份数据,看看有没有;
            Looter bakLooter = bakOfflineLooterMap.get(id);
            this.looters.put(id, looter);
            if (bakLooter != null) {
                looter.pvpLtDamageRankVo.addAddedDamage(bakLooter.pvpLtDamageRankVo.getDamage());
                ltDamageRank.setDamage(looter.pvpLtDamageRankVo);
            }
            if (step == STEP_FIGHT) {
                //设置匹配时间并加入匹配对待队列
                looter.setNextMatchTime(System.currentTimeMillis());
                this.waitMatch.addLast(looter);
                LootTreasureManager.log("夺宝服 设置玩家id=" + id + " 匹配时间并加入匹配对待队列");
            }
        } else {
            if (step == STEP_FIGHT) {
                if (offlineLooters.contains(id)) {
                    offlineLooters.remove(id);
                }
                Looter looter = looters.get(id);
                LTRoom ltRoom = looter.getRoom();
                if (ltRoom != null) {
                    newFighterToFightServer(ltRoom, looter);
                }
            }
        }
    }


    @Override
    public void stopSelf() {
        runner.runnable = false;
    }


    /**
     * @param looterId looterId死亡
     */
    public void dead(long looterId, long killer) {
        checkKillOne(looterId, killer);
        LogUtil.info("dead handle2 deadRoleId=" + looterId);
        Looter looter = looters.get(looterId);
        looter.getRoom().removeLooter(looterId);
        looter.recycle();
        if (offlineLooters.contains(looterId)) {
            //已离线
            if (looter.getBoxs() <= 0) {
                LootTreasureManager.log("夺宝服：移除玩家:" + looterId);
                bakOfflineLooterMap.put(looterId, looter);
                looters.remove(looterId);
                offlineLooters.remove(looterId);
                RoleId2ActorIdManager.remove(looterId);
                return;
            } else {
                revive(looterId);
            }
        } else {
            MultiServerHelper.modifyConnectorRoute(looterId, serverId);
            revive(looterId);
            //死亡时通知客户端清理一下其它玩家
            PacketManager.send(looterId, new ClientClearOtherFighters());
//            PacketManager.send(looterId, new ClientText("您已经死亡，请等待重新分配房间"));
        }

        //死亡后5S秒才可以再继续匹配，加入等待匹配队列
        looter.setNextMatchTime(System.currentTimeMillis() + DEAD_MATCH_TIME);
        this.waitMatch.addFirst(looter);
    }

    @Override
    public void revive(long roleId) {
        Looter looter = this.looters.get(roleId);
        if (looter == null) {
            try {
                throw new Exception("要复活的玩家不在looters里!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
//        //先满血复活
//        ClientUpdatePlayer clientUpdatePlayer = new ClientUpdatePlayer();
//        ArrayList<FighterEntity> fArrayList2 = new ArrayList<FighterEntity>();
//        fArrayList2.add(looter.getFiEntity());
//        clientUpdatePlayer.setNewFighter(fArrayList2);
//        PacketManager.send(looter.getId(), clientUpdatePlayer);
        looter.getFiEntity().changeHp(looter.getFiEntity().getAttribute().getMaxhp());
        //通知客户端复活了;
        ClientRoleRevive clientRoleRevive = new ClientRoleRevive(roleId, true);
        PacketManager.send(roleId, clientRoleRevive);
    }

    public void doLuaFramData(LuaFrameData lFrameData) {
        if (lFrameData.getDamage() != null) {
            //伤害的处理
            long hitedId;
            Looter looter;
            for (Map.Entry<String, HashMap<String, Integer>> kvp : lFrameData.getDamage().entrySet()) {
                hitedId = Long.parseLong(kvp.getKey());
                for (Map.Entry<String, Integer> subKvp : kvp.getValue().entrySet()) {
                    if (Long.parseLong(subKvp.getKey()) == Long.parseLong(kvp.getKey())) {
                        LogUtil.error("自己砍自己! 阵营配置有问题");
                    }
                    looter = this.looters.get(hitedId);
                    if (looter != null) {
                        looter.getFiEntity().changeHp(subKvp.getValue());
                    }
                    hurt(Long.parseLong(subKvp.getKey()), hitedId, subKvp.getValue());
                }
            }
        }
        if (lFrameData.getDead() != null) {
            Looter looter;
            //处理死亡
            HashMap<String, String> dead = lFrameData.getDead();
            Set<Entry<String, String>> set = dead.entrySet();
            for (Entry<String, String> entry : set) {
                long roleId = Long.parseLong(entry.getKey());
                long killer = Long.parseLong(entry.getValue());
                looter = this.looters.get(roleId);
                if (looter != null) {
                    looter.getFiEntity().changeHp(-(looter.getFiEntity().getAttribute().getHp()));
                }
                this.dead(roleId, killer);
            }
        }
    }


    /**
     * 从受害者记的攻击者列表记录中移除攻击者记录;
     *
     * @param attackerId
     */
    public void removeAttakerFormHittedMap(long attackerId) {
        for (Map.Entry<Long, Looter> kvp : looters.entrySet()) {
            kvp.getValue().removeHurt(attackerId);
        }
    }


    /**
     * 检测击杀了一个人;
     */
    public void checkKillOne(long deadLooterId, long killerId) {
        Looter deadLooter = this.looters.get(deadLooterId);
        Looter killLooter = this.looters.get(killerId);
        //记录杀人排行榜数据;
        if (deadLooter == null || killLooter == null) {
            return;
        }
        int loseCount = 0;
        do {
            //调试日志start
            if (LootTreasureConstant.DEBUG) {
                LootTreasureManager.log("夺宝服  " + killLooter.getFiEntity().getName() + "击杀了：" + deadLooter.getFiEntity().getName());
                Map<Long, Integer> logHurterMap = getCurrentHurtMap(deadLooterId);
                Looter tmpLooter = null;
                for (Map.Entry<Long, Integer> kvp : logHurterMap.entrySet()) {
                    tmpLooter = this.looters.get(kvp.getKey());
                    LootTreasureManager.log("夺宝服  " + tmpLooter.getFiEntity().getName() + " 攻击过: " + deadLooter.getFiEntity().getName());
                }
            }
            //调试日志end

            //移除对应的伤害列表;
            removeAttakerFormHittedMap(deadLooterId);
            //获取符合条件的攻击者列表;
            Map<Long, Integer> hurterMap = getCurrentHurtMap(deadLooterId);
            if (hurterMap != null) {
                Looter tmpLooter = null;
                int realGainCount = 0;
                //攻击者也要计算一次"杀人数"
                for (Map.Entry<Long, Integer> kvp : hurterMap.entrySet()) {
                    tmpLooter = this.looters.get(kvp.getKey());
                    tmpLooter.pvpLtDamageRankVo.addAddedDamage(1);
                    realGainCount = tmpLooter.gainBoxCount(1);
                    ltDamageRank.setDamage(tmpLooter.pvpLtDamageRankVo);
                    syncToClientGainBoxCount(tmpLooter, realGainCount);
                }
                //刷新排名索引;
                refreshRankSortIndex();
                //同步排行榜数据给对应的攻击者客户端;
                for (Map.Entry<Long, Integer> kvp : hurterMap.entrySet()) {
                    tmpLooter = this.looters.get(kvp.getKey());
                    rankSort(true, tmpLooter.getId(), false);
                }
                //同步排行榜数据给对应的受害者客户端;
                rankSort(true, deadLooter.getId(), false);

//                loseCount = LootTreasureManager.getLoseCountByCurrentBox(deadLooter.getBoxs());
//                //获取丢失的宝箱数;
//                if (loseCount <= 0) {
//                    break;
//                } else {
//                    //做个兼容,放置数据配置有问题，大于玩家自身拥有的宝箱数;fi
//                    loseCount = deadLooter.getBoxs() < loseCount ? deadLooter.getBoxs() : loseCount;
//                }
//                int remainCount = loseCount;
//                //因为每个人至多有一个宝箱,那么就不需要用权值计算了,每人一个就行;
//                int realGainCount = 0;
//                if (loseCount >= hurterMap.size()) {
//                    for (Map.Entry<Long, Integer> kvp : hurterMap.entrySet()) {
//                        tmpLooter = this.looters.get(kvp.getKey());
//                        if (!tmpLooter.isBoxLimit()) {
//                            realGainCount = tmpLooter.gainBoxCount(1);
//                            remainCount -= realGainCount;
//                            syncToClientGainBoxCount(tmpLooter, realGainCount);
//                        }
//                    }
//                } else {
//                    //这里就需要计算权值了;
//                    int randomTotalValue = 0;
//                    List<Looter> tmpLooterList = new ArrayList<>();
//                    for (Map.Entry<Long, Integer> kvp : hurterMap.entrySet()) {
//                        tmpLooter = this.looters.get(kvp.getKey());
//                        if (!tmpLooter.isBoxLimit()) {
//                            tmpLooterList.add(tmpLooter);
//                            randomTotalValue += LootTreasureManager.getBoxCountFormularRate(tmpLooter.getBoxs());
//                        }
//                    }
//                    int selectIndex = -1;
//                    if (randomTotalValue > 0) {
//                        for (int i = 0, len = remainCount; i < len; i++) {
//                            selectIndex = randomRateLooters(tmpLooterList, randomTotalValue);
//                            if (selectIndex >= 0) {
//                                tmpLooter = tmpLooterList.get(selectIndex);
//                                if (!tmpLooter.isBoxLimit()) {
//                                    realGainCount = tmpLooter.gainBoxCount(1);
//                                    remainCount -= realGainCount;
//                                    tmpLooterList.remove(selectIndex);
//                                    syncToClientGainBoxCount(tmpLooter, realGainCount);
//                                }
//                            }
//                        }
//                    }
//                }
//                loseCount = loseCount - remainCount;
            }
//            新版规则宝箱改为积分,并且积分不会减少;
//            if (deadLooter.loseBoxCount(loseCount)) {
//                syncToClientLoseBoxCount(deadLooter, loseCount);
//            }
        } while (false);
        //要判断是否在线;
        if (!offlineLooters.contains(killLooter.getId())) {
            //通知击杀着客户端来了个致命一击;
            ClientLootTreasureOpr clientLootTreasureOpr = new ClientLootTreasureOpr(ClientLootTreasureOpr.KILL);
            clientLootTreasureOpr.roleName = deadLooter.getFiEntity().getName();
            sendToClient(killLooter, clientLootTreasureOpr);
        }
        if (!offlineLooters.contains(deadLooter.getId())) {
            //通知被击杀着客户端来了个致命一击;
            ClientLootTreasureOpr clientLootTreasureOpr = new ClientLootTreasureOpr(ClientLootTreasureOpr.BE_KILLED);
            clientLootTreasureOpr.roleName = killLooter.getFiEntity().getName();
            clientLootTreasureOpr.count = (short)loseCount;
            sendToClient(deadLooter, clientLootTreasureOpr);
        }
        //通知房间内的客户端宝箱变动;
        syncToClientBoxCount(deadLooter.getRoom());
    }

    private void syncToClientGainBoxCount(Looter looter, int realGainCount) {
        if (realGainCount > 0) {
            ClientLootTreasureOpr clientLootTreasureOpr = new ClientLootTreasureOpr(ClientLootTreasureOpr.GAINBOX);
            clientLootTreasureOpr.count = (short)realGainCount;
            sendToClient(looter, clientLootTreasureOpr);
        }
    }

    private void syncToClientLoseBoxCount(Looter looter, int realLoseCount) {
        if (realLoseCount > 0) {
            ClientLootTreasureOpr clientLootTreasureOpr = new ClientLootTreasureOpr(ClientLootTreasureOpr.LOSEBOX);
            clientLootTreasureOpr.count = (short)realLoseCount;
            sendToClient(looter, clientLootTreasureOpr);
        }
    }


    private int randomRateLooters(List<Looter> looterList, int rateTotalValue) {
        //随机;
        int randomValue = new Random().nextInt(rateTotalValue);
        int tmpSumValue = 0;
        for (int i = 0, len = looterList.size(); i < len; i++) {
            tmpSumValue += LootTreasureManager.getBoxCountFormularRate(looterList.get(i).getBoxs());
            if (randomValue <= tmpSumValue) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 注意:确保调用时,玩家还在当前房间中;
     */
    public Map<Long, Integer> getCurrentHurtMap(long deadRoleId) {
        Looter deadLooter = looters.get(deadRoleId);
        if (deadLooter == null) {
            LogUtil.error("要获取伤害者的死亡者自己并不在记录的looters集合里!");
            return null;
        }
        Map<Long, Integer> srcAttackerMap = deadLooter.hurts;
        if (srcAttackerMap != null) {
            Map<Long, Integer> rtnAttackerMap = new ConcurrentHashMap<>();
            //判断是否和deadRole在同一房间中;
            Looter tmpLooter = null;
            for (Map.Entry<Long, Integer> kvp : srcAttackerMap.entrySet()) {
                tmpLooter = looters.get(kvp.getKey());
                if (tmpLooter != null && tmpLooter.getRoom() != null && deadLooter.getRoom() != null && tmpLooter.getRoom().getId() == deadLooter.getRoom().getId()) {
                    rtnAttackerMap.put(tmpLooter.getId(), kvp.getValue());
                }
            }
            return rtnAttackerMap;
        }
        return null;
    }


    /**
     * @param initiator
     * @param victim    initiator对victim造成伤害
     */
    public void hurt(long initiator, long victim, int count) {
        Looter looter = looters.get(victim);
        looter.addHurt(initiator, count);
    }

    private void syncLooterSwitchedRoom(Looter looter, byte roomType, int diffBoxCount, boolean isReally) {
        ClientLootTreasureInfo clientLootTreasureInfo = new ClientLootTreasureInfo(ClientLootTreasureInfo.TYPE_SWITCH_ROOM_RTN);
        clientLootTreasureInfo.setRoomTypeIsReallySwitched(isReally);
        clientLootTreasureInfo.setRoomType(roomType);
        clientLootTreasureInfo.setRoomDiffBoxCount(diffBoxCount);
        clientLootTreasureInfo.setSwitchRoomEndCdStamp(looter.getLastManualSwitchRoomEndStamp());
        sendToClient(looter, clientLootTreasureInfo);
    }

    /**
     * @param looter
     * @param rooms  匹配房间
     */
    public boolean matchRoom(Looter looter, LinkedList<LTRoom> rooms) {
        try {

            long now = System.currentTimeMillis();
            boolean createRooming = false;//是否有新的房间在创建中
            LogUtil.info("match role: " + looter.getId());
            for (LTRoom ltRoom : rooms) {
                if (ltRoom.size() >= LootTreasureManager.PERSON_LIMITCOUNT_PER_ROOM) {
                    //人数已满
                    LootTreasureManager.log("夺宝服 匹配房间[" + ltRoom.getId() + "]：人数已满");
                    continue;
                }
                //加入lootSection的matching字段进行判断;
                Looter topLevelLooter = ltRoom.getMaster();
                if (topLevelLooter != null) {
                    int matchLevelDiffValue = topLevelLooter.getFiEntity().getLevel() - looter.getFiEntity().getLevel();
                    if (matchLevelDiffValue < 0 || matchLevelDiffValue > lootSectionVo.getMatching()) {
                        //不满足lootsection的matching字段;
                        LootTreasureManager.log("夺宝服 不满足匹配房间, matching差值[" + matchLevelDiffValue + "]");
                        continue;
                    }
                }

                if (ltRoom.getCreateActorTimeDelay() != 0) {
                    //房间刚创建还在等待战斗服返回中
                    createRooming = true;
                    LootTreasureManager.log("夺宝服 匹配房间[" + ltRoom.getId() + "]：房间刚创建还在等待战斗服返回中");
                    continue;
                }
                ltRoom.addLooter(looter);
                looter.recycle();
                looter.setRoom(ltRoom);
                ltRoom.setLastActivityTime(now);
//                newFighterToFightServer(ltRoom, looter);
                addFighterToFightServer(ltRoom.getFightActor(), looter, ltRoom.getFightServer());
                syncLooterSwitchedRoom(looter, looter.getRoom_flag(), 0, true);
                LootTreasureManager.log("夺宝服 匹配房间[" + ltRoom.getId() + "]：成功");

                // 匹配成功下包
                ClientLootTreasureOpr res = new ClientLootTreasureOpr(ClientLootTreasureOpr.MATCH_SUC);
                PacketManager.send(looter.getId(), res);

                return true;
            }
            if (createRooming) {
                //先不匹配了，等待房间创建
                LootTreasureManager.log("夺宝服 先不匹配了，等待房间创建");
                return false;
            }

            LTRoom ltRoom = new LTRoom(++roomIdCounter);
            //设置超时回收时间，也就是在5s内匹配不到玩家，那么这个房间将会被回收
            ltRoom.setRecycleTime(now + IDLE_ROOM_RECYCEL_TIME);
            rooms.addLast(ltRoom);
            LootTreasureManager.log("夺宝服 创建房间[" + ltRoom.getId() + "]：成功");
            LootTreasureManager.log("夺宝服 房间数：" + rooms.size());
            rooms_all.put(ltRoom.getId(), ltRoom);
            //给创建Actor 5秒的时间，超过5s则认为没有创建成功
            ltRoom.setCreateActorTimeDelay(now + CREATE_FACTOR_DELAY);
            int fightServer = ServiceHelper.fsManagerService().getFightServer(FSManagerServiceActor.FIGHT_SERVER_LEVEL_COMM);
            ltRoom.setFightServer(fightServer);
            //请求战斗服创建一场战斗
            Collection<FighterEntity> fighterEntityColl = new ArrayList<FighterEntity>();
            RMLTRPCHelper.fightBaseService.createFight(
                    fightServer,
                    FightConst.T_LOOT_TREASURE, serverId,
                    RMLTServiceActor.createFightId(), LTActor.getClientEnterPKData(LTActor.newClientEnterPK(fighterEntityColl)),
                    ltActor.getId() + "|" + ltRoom.getId());
            return false;
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            if (looter.getRoom() != null) {
                looter.getRoom().removeLooter(looter.getId());
                looter.setRoom(null);
            }
            return false;
        }
    }

    private void addFighterToFightServer(String fightId, Looter looter, int fightServer) {
        List<FighterEntity> fighterEntityList = new ArrayList<>();
        looter.getFiEntity().fighterType = FighterEntity.TYPE_PLAYER;
        FighterEntity cloneFightEntity = looter.getFiEntity().copy();
        //判断是否离线;
        if (offlineLooters.contains(looter.getId())) {
            cloneFightEntity.setState((byte) 1);
        } else {
            cloneFightEntity.setState((byte) 0);
        }
        fighterEntityList.add(cloneFightEntity);
        RMLTRPCHelper.fightBaseService().addFighter(fightServer, FightConst.T_LOOT_TREASURE,
                serverId, fightId, fighterEntityList);
    }


    public void newFighterToFightServer(LTRoom ltRoom, Looter looter) {
        //战斗相关的处理
        //通知战斗服有新的fighter加入
        addFighterToFightServer(ltRoom.getFightActor(), looter, ltRoom.getFightServer());

        //需要通知原有房间里的人,该玩家的宝箱数;
        ClientLootTreasureOpr clientLootTreasureOpr = new ClientLootTreasureOpr(ClientLootTreasureOpr.ROOMBOX);
        clientLootTreasureOpr.addBoxCount(looter.getId(), looter.getBoxs());
        Map<Long, Looter> looterMap = ltRoom.getLooters();
        for (Map.Entry<Long, Looter> kvp : looterMap.entrySet()) {
            if (kvp.getKey() != looter.getId()) {
                //判断是否在线;
                if (!offlineLooters.contains(kvp.getKey())) {
                    sendToClient(kvp.getValue(), clientLootTreasureOpr);
                }
            }
        }
        //下面是调试日志;
        LootTreasureManager.log("夺宝服 通知战斗服有新的fighter加入 roleid=" + looter.getId() + ", roomid=" + ltRoom.getId() + ",actorid=" + ltRoom.getFightActor());
        String str = "";
        looterMap = ltRoom.getLooters();
        for (Map.Entry<Long, Looter> kvp : looterMap.entrySet()) {
            str += kvp.getValue().getId() + "|" + kvp.getValue().getFiEntity().getName() + " , ";
        }
        LootTreasureManager.log("夺宝服 房间[" + ltRoom.getId() + "] 信息:" + str);
    }

    /**
     * 匹配
     */
    public void matchRoom() {
        if (waitMatch == null || waitMatch.size() <= 0) {
            return;
        }

        long now = System.currentTimeMillis();
        List<Looter> list = new ArrayList<Looter>();
        try {
            Looter looter = waitMatch.getLast();
            while (looter != null) {
                boolean isValid = looter.getNextMatchTime() <= now;
                if (isValid) {
                    waitMatch.removeLast();
                    LootTreasureManager.log("夺宝服 检查房间匹配: " + looter.getId());
                    //检查房间切换;
                    looter.checkSwitchRoomFlag();
                    if (!matchRoom(looter, looter.getRoom_flag() == ROOM_FLAG_HIGH ? rooms_high : rooms_low)) {
                        list.add(looter);
                    }
                    if (waitMatch.size() <= 0) {
                        break;
                    }
                } else {
                    break;
                }
                //等待队列已按照匹配时间排好，从尾部拿
                looter = waitMatch.getLast();
            }
            if (list.size() > 0) {
                for (Looter lt : list) {
                    waitMatch.addLast(lt);
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }

    }

    private void recycleRoom(LTRoom ltRoom) {
        Collection<Looter> col = ltRoom.getLooters().values();
        boolean clearOther = ltRoom.size() > 1 ? true : false;
        for (Looter looter : col) {
            //这里还需要重置客户端状态
            looter.setNextMatchTime(System.currentTimeMillis());
            this.waitMatch.addLast(looter);
            if (!this.offlineLooters.contains(looter.getId())) {
                //把路由改回来
                MultiServerHelper.modifyConnectorRoute(looter.getId(), serverId);
            }
            if (clearOther) {
                //把客户端的其它玩家清掉
                sendToClient(looter.getId(), new ClientClearOtherFighters());
            }
        }
        ltRoom.recycle();
    }

    /**
     * @param rooms 如果房间里面没有人，或者只有一个人超过一段时间后
     */
    public void recycleRoom(LinkedList<LTRoom> rooms) {
        try {
            long now = System.currentTimeMillis();
            List<LTRoom> removes = null;

            for (LTRoom ltRoom : rooms) {
                if (ltRoom.size() == 0 && ltRoom.getRecycleTime() == 0 && ltRoom.getLastActivityTime() == 0) {
                    LogUtil.info("recycel room roomid=" + ltRoom.getId() + ",size=0");
                    recycleRoom(ltRoom);
                } else if (ltRoom.size() <= 1 && ltRoom.getRecycleTime() != 0 && ltRoom.getRecycleTime() <= now) {
                    LogUtil.info("recycel room roomid=" + ltRoom.getId() + ",recycleTime");
                    recycleRoom(ltRoom);
                } else if (ltRoom.getCreateActorTimeDelay() != 0 && ltRoom.getCreateActorTimeDelay() <= now) {
                    LogUtil.info("recycel room roomid=" + ltRoom.getId() + ",createActorTime");
                    recycleRoom(ltRoom);
                } else if (ltRoom.getLastActivityTime() != 0 && now - ltRoom.getLastActivityTime() >= ROOM_ACTIVITY_TIME) {
                    LogUtil.info("recycel room roomid=" + ltRoom.getId() + ",inActivityTime");
                    recycleRoom(ltRoom);
                    //一段时间不活跃了，可能是fightActor出了问题，所以这个房间不能保留了
//					continue;
                } else {
                    continue;
                }
                if (removes == null) {
                    removes = new ArrayList<LTRoom>();
                }
                removes.add(ltRoom);
            }
            if (removes != null) {
                for (LTRoom ltRoom : removes) {
//                	ltRoom.setLastActivityTime(0);
                    boolean f = rooms.remove(ltRoom);
                    LootTreasureManager.log("夺宝服 移除房间 flag = " + f + ",roomid=" + ltRoom.getId());
                    rooms_all.remove(ltRoom.getId());
                    if (ltRoom.getFightActor() != null) {
//                        sendToFightServer(new StopFightActor(ltRoom.getFightActor()));
                        RMLTRPCHelper.fightBaseService().stopFight(ltRoom.getFightServer(), FightConst.T_LOOT_TREASURE,
                                serverId, ltRoom.getFightActor());
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }


    }

    /**
     * 夺宝活动状态维护
     */
    public void maintenState() {
        try {
            long now = System.currentTimeMillis();
            if (now >= nextStepTime) {
                if (step == STEP_WAIT) {
                    step = STEP_FIGHT;
                    Random random = new Random(now);
                    nextStepTime = now + LootTreasureManager.PVP_FIGHT_TIME + random.nextInt(1000);//加个随机值 避免所有专区同时发奖导致流量过高
                    String nowSdf = new SimpleDateFormat(DateUtil.getYMDHMS_Str()).format(new Date(nextStepTime));
                    LootTreasureManager.log("夺宝活动进入PVP战斗阶段了! 结束时间为:  " + nowSdf);
                    Collection<Looter> col = this.looters.values();
                    ClientLootTreasureInfo clientLootTreasureInfo = new ClientLootTreasureInfo(ClientLootTreasureInfo.TYPE_ACTIVITY_NOTICE);
                    clientLootTreasureInfo.setActivitySegment(LootTreasureConstant.ACTIVITYSEGMENT.PVP_START);
                    clientLootTreasureInfo.setStartStamp(lootTreasureStartup.lFlow.getStartTimeStamp());
                    clientLootTreasureInfo.setEndStamp(lootTreasureStartup.lFlow.getEndTimeStamp());
                    for (Looter looter : col) {
                        looter.getFiEntity().fighterType = FighterEntity.TYPE_PLAYER;
                        if (!offlineLooters.contains(looter.getId())) {
                            PacketManager.send(looter.getId(), LTActor.newClientEnterPK(looter.getFiEntity()));
                            //通知客户端PVP阶段开始了;
                            sendToClient(looter, clientLootTreasureInfo);
                        }
                        looter.setNextMatchTime(now);
                        waitMatch.addLast(looter);
                    }
                } else if (step == STEP_FIGHT) {
                    LootTreasureManager.log("夺宝活动结束，计算奖励准备发放  " + LootTreasureManager.PVP_OVER_WAIT_TIME + "秒");
                    step = STEP_OVER;
                    nextStepTime = now + LootTreasureManager.PVP_OVER_WAIT_TIME;
                    //停止战斗服的战斗
                    stopAllFight();
                    //活动结束,计算奖励并发放;
                    checkAwardToLooters();
                } else if (step == STEP_OVER) {
                    LootTreasureManager.log("夺宝活动的资源回收,踢人等清理工作  ");
                    //夺宝活动的资源回收,踢人等清理工作;
                    finish();
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    //结算奖励;
    private void checkAwardToLooters() {
        Collection<Looter> col = this.looters.values();
        Integer tmpAffixId = null;
        for (Looter looter : col) {
            //计算奖励;
            tmpAffixId = lootSectionVo.awardMap.get(looter.getBoxs());
            if(tmpAffixId != null){
                // 执行掉落
                Map<Integer, Integer> dropMap = DropManager.executeDrop(tmpAffixId, 1);
                //发送到主服邮件;
                RMLTRPCHelper.lootTreasureService().sendAwardEmail(
                        looter.getServerId(),
                        MConst.CCLootTreasure,
                        looter.getId(),
                        LootTreasureManager.OVER_EMAIL_TEMPLATE_ID,
                        0,
                        "系统",
                        dropMap);
                if (isOnline(looter.getId())) {
                    //要发送到客户端进行奖励预览;
//                    ClientLootTreasureInfo clientLootTreasureInfo = new ClientLootTreasureInfo(ClientLootTreasureInfo.TYPE_END_AWARDS);
//                    clientLootTreasureInfo.setRewardMap(dropMap);
//                    PacketManager.send(looter.getId(), clientLootTreasureInfo);

                    ClientStageFinish finish = new ClientStageFinish(SceneManager.SCENETYPE_LOOTTREASURE_PVP, ClientStageFinish.VICT);
                    finish.setItemMap(dropMap);
                    PacketManager.send(looter.getId(), finish);
                }
            }
        }
    }

    public void finish(LinkedList<LTRoom> rooms) {
        for (LTRoom ltRoom : rooms) {
            ltRoom.recycle();
            if (ltRoom.getFightActor() != null) {
//                sendToFightServer(new StopFightActor(ltRoom.getFightActor()));
                RMLTRPCHelper.fightBaseService().stopFight(ltRoom.getFightServer(), FightConst.T_LOOT_TREASURE,
                        serverId, ltRoom.getFightActor());
            }
        }
        rooms.clear();
    }

    public void finish() {
        //再次同步一次排行榜数据到客户端;
        checkSyncRankDataToMainServer();
        stopSelf();
        offlineLooters.clear();
        int lootSize = looters == null ? 0 : looters.size();
        LogUtil.info("夺宝pvp活动结束: 人数" + lootSize);
        //将玩家弄回城;
        ServerExitFight serverExitFight = new ServerExitFight();
        long looterId = 0;
        for (Map.Entry<Long, Looter> kvp : looters.entrySet()) {
            looterId = kvp.getValue().getId();
            serverExitFight.setRoleId(looterId);
            onReceived(serverExitFight);
            SessionManager.remove(looterId);
            RoleId2ActorIdManager.remove(looterId);
        }
        looters.clear();
        bakOfflineLooterMap.clear();
        finish(rooms_high);
        finish(rooms_low);
        rooms_all.clear();
        ActorServer.getActorSystem().removeActor(ltActor.getId());
        this.ltActor.setLootTreasure(null);
        this.ltActor = null;
    }
    
    public void stopAllFight(){
    	try {
    		for (LTRoom ltRoom : rooms_low) {
                if (ltRoom.getFightActor() != null) {
                    RMLTRPCHelper.fightBaseService().stopFight(ltRoom.getFightServer(), FightConst.T_LOOT_TREASURE,
                            serverId, ltRoom.getFightActor());
                }
            }
        	for (LTRoom ltRoom : rooms_high) {
                if (ltRoom.getFightActor() != null) {
                    RMLTRPCHelper.fightBaseService().stopFight(ltRoom.getFightServer(), FightConst.T_LOOT_TREASURE,
                            serverId, ltRoom.getFightActor());
                }
            }
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
    	
    }

    //检查同步排行榜数据到主服中;
    private void checkSyncRankDataToMainServer() {
        preSyncRankToMainTime++;
        if (preSyncRankToMainTime > gapSyncRankToMain) {
            if (ltDamageRank.isHasChange()) {
                preSyncRankToMainTime = 0;
                RMLTServiceActor.syncRankListToAllServers(this.ltActor.getId(), ltDamageRank);
                ltDamageRank.setHasChange(false);
            }
        }
    }

    public void doRun() {
        maintenState();
        if (step == STEP_FIGHT) {
            recycleRoom(rooms_high);
            recycleRoom(rooms_low);
            matchRoom();
            checkSyncRankDataToMainServer();
        }
        //测试;
//		for (Map.Entry<Long, Map<Long, Integer>> kvp : damageMap.entrySet()){
//		}
    }

    private void syncToClientBoxCount(LTRoom ltRoom) {
        syncToClientBoxCount(ltRoom, null, null);
    }

    /**
     * 同步到客户端更新玩家的宝箱数量显示; 如果要设置deadLooter的话，那么killLooter也要设置，反过来也一样;
     */
    private void syncToClientBoxCount(LTRoom ltRoom, Looter deadLooter, Looter killLooter) {
        ClientLootTreasureOpr clientLootTreasureOpr = new ClientLootTreasureOpr(ClientLootTreasureOpr.ROOMBOX);
        Map<Long, Looter> looterMap = ltRoom.getLooters();
        Looter tmpLooter = null;
        if (deadLooter == null && killLooter == null) {
            //将当前房间的人员宝箱信息进行收集;
            for (Map.Entry<Long, Looter> kvp : looterMap.entrySet()) {
                tmpLooter = kvp.getValue();
                clientLootTreasureOpr.addBoxCount(tmpLooter.getId(), tmpLooter.getBoxs());
            }
        } else {
            clientLootTreasureOpr.addBoxCount(deadLooter.getId(), deadLooter.getBoxs());
            clientLootTreasureOpr.addBoxCount(killLooter.getId(), killLooter.getBoxs());
        }
        //发送给房间的人员;
        for (Map.Entry<Long, Looter> kvp : looterMap.entrySet()) {
            tmpLooter = kvp.getValue();
            //判断是否在线;
            if (!offlineLooters.contains(kvp.getKey())) {
                sendToClient(tmpLooter, clientLootTreasureOpr);
            }
        }
    }

    private void refreshRankSortIndex() {
        ltDamageRank.sortIndex();
    }

    private void rankSort(boolean needSyncToClient, long specialLooterId, boolean needSetSortIndex) {
        //排序号;
        if (needSetSortIndex) {
            refreshRankSortIndex();
        }
        if (needSyncToClient) {
            List<LTDamageRankVo> firstRankVoList = ltDamageRank.getFirstList(RANK_CLIENT_SHOW_COUNT);
            //发送击杀排行榜数据到参与的客户端;
            ClientLootTreasureRankList clientLootTreasureRankList = new ClientLootTreasureRankList(SceneManager.SCENETYPE_LOOTTREASURE_PVP, firstRankVoList);
            if (specialLooterId < 0) {
                for (Map.Entry<Long, Looter> kvp : looters.entrySet()) {
                    clientLootTreasureRankList.setMySelfRankVo(kvp.getValue().pvpLtDamageRankVo);
                    sendToClient(kvp.getValue(), clientLootTreasureRankList);
                }
            } else {
                Looter looter = this.looters.get(specialLooterId);
                if (looter != null) {
                    clientLootTreasureRankList.setMySelfRankVo(looter.pvpLtDamageRankVo);
                    sendToClient(looter, clientLootTreasureRankList);
                }
            }
        }
    }

    class PVPLootTreasureRunner extends Thread {

        private boolean runnable = false;

        private RunEvent rEvent;

        public PVPLootTreasureRunner() {
            rEvent = new RunEvent(1);
        }

        @Override
        public void run() {
            while (runnable) {
                try {
                    ltActor.tell(rEvent, Actor.noSender);
                } catch (Exception e) {
                    LogUtil.error(e.getMessage(), e);
                }
                //一秒跑一次
                doSleep(1000l);
            }
        }

        private void doSleep(long time) {
            try {
                sleep(time);
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        }

        public RunEvent getrEvent() {
            return rEvent;
        }
    }

}
