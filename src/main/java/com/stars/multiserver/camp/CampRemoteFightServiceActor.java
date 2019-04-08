package com.stars.multiserver.camp;

import com.stars.ExcutorKey;
import com.stars.core.schedule.SchedulerManager;
import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.activity.CampActivity;
import com.stars.modules.camp.event.ActivityFinishEvent;
import com.stars.modules.camp.event.CampFightEvent;
import com.stars.modules.camp.packet.ClientCampFightPacket;
import com.stars.modules.camp.pojo.CampFightGrowUP;
import com.stars.modules.camp.pojo.CampFightSingleScoreReward;
import com.stars.modules.camp.prodata.CampActivityVo;
import com.stars.modules.camp.prodata.CampGrade;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.pk.packet.ClientUpdatePlayer;
import com.stars.modules.pk.packet.ModifyConnectorRoute;
import com.stars.modules.pk.packet.ServerOrder;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.camp.pojo.CampFightMatchInfo;
import com.stars.multiserver.fight.ServerOrders;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.multiserver.fightutil.campfight.CampBattle;
import com.stars.network.PacketUtil;
import com.stars.network.server.packet.Packet;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.fightServerManager.FSManagerServiceActor;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by huwenjun on 2017/7/20.
 */
public class CampRemoteFightServiceActor extends ServiceActor implements CampRemoteFightService {
    private Map<Long, CampFightMatchInfo>[] campMatchingMap = new LinkedHashMap[3];
    private int[] campFightingNum = new int[3];
    private Map<Long, CampFightMatchInfo> campFightingMap = new LinkedHashMap();
    private Map<String, CampBattle> campFightRoomMap = new ConcurrentHashMap<>();
    private Map<Long, String> roleRoomMap = new HashMap<>();

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.campRemoteFightService, this);
        campMatchingMap[CampManager.CAMPTYPE_QIN] = new LinkedHashMap<>();
        campMatchingMap[CampManager.CAMPTYPE_CHU] = new LinkedHashMap<>();
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.SCHEDULE_KEY_MATCH, new Runnable() {
            @Override
            public void run() {
                ServiceHelper.campRemoteFightService().handleMatch();
            }
        }, 10, CampManager.campActivity2Matchtime, TimeUnit.SECONDS);
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.SCHEDULE_KEY_DESTROY_ROOM, new Runnable() {
            @Override
            public void run() {
                ServiceHelper.campRemoteFightService().destroyRoom();
            }
        }, 10, 3, TimeUnit.SECONDS);
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.SCHEDULE_KEY_CAMP_FIGHT_LOG, new Runnable() {
            @Override
            public void run() {
                ServiceHelper.campRemoteFightService().logRoomInfo();
            }
        }, 10, 4, TimeUnit.SECONDS);
    }

    @Override
    public void printState() {

    }

    /**
     * 加入匹配队列
     *
     * @param campServerId
     * @param campFightMatchInfo
     */
    @Override
    public void startMatching(int campServerId, CampFightMatchInfo campFightMatchInfo) {
        com.stars.util.LogUtil.info("roleId:{} enter matching pool:{}", campFightMatchInfo.getRoleId(), campFightMatchInfo.getCampType());
        int campType = campFightMatchInfo.getCampType();
        if (!campFightingMap.containsKey(campFightMatchInfo.getRoleId())) {
            campFightingNum[campType] += 1;
        }
        campFightingMap.put(campFightMatchInfo.getRoleId(), campFightMatchInfo);
        campMatchingMap[campType].put(campFightMatchInfo.getRoleId(), campFightMatchInfo);
        ClientCampFightPacket clientCampFightPacket = new ClientCampFightPacket(ClientCampFightPacket.SEND_START_MATCHING);
        CampRpcHelper.roleService().send(campFightMatchInfo.getFromServerId(), campFightMatchInfo.getRoleId(), clientCampFightPacket);
    }

    /**
     * 取消匹配
     *
     * @param campServerId
     * @param campFightMatchInfo
     */
    @Override
    public void cancelMatching(int campServerId, CampFightMatchInfo campFightMatchInfo) {
        com.stars.util.LogUtil.info("roleId:{} cancel match", campFightMatchInfo.getRoleId());
        int campType = campFightMatchInfo.getCampType();
        campMatchingMap[campType].remove(campFightMatchInfo.getRoleId());
        long roleId = campFightMatchInfo.getRoleId();
        if (campFightingMap.containsKey(roleId)) {
            campFightingMap.remove(roleId);
            campFightingNum[campFightMatchInfo.getCampType()] -= 1;
        }
        String roomId = roleRoomMap.get(roleId);
        roleRoomMap.remove(roleId);
        if (roomId != null) {
            CampBattle campBattle = campFightRoomMap.get(roomId);
            if (campBattle != null) {
                campBattle.leaveRoom(roleId + "", false);
            }
        }
        ClientCampFightPacket clientCampFightPacket = new ClientCampFightPacket(ClientCampFightPacket.SEND_CANCEL_MATCHING);
        CampRpcHelper.roleService().send(campFightMatchInfo.getFromServerId(), campFightMatchInfo.getRoleId(), clientCampFightPacket);
    }

    /**
     * 匹配线程
     * 负责处理匹配逻辑
     */
    @Override
    public void handleMatch() {
        com.stars.util.LogUtil.info("begin handle match pool");
        applyBattle();
        List<CampBattle> campBattles = new ArrayList<>(campFightRoomMap.values());
        Collections.sort(campBattles);
        for (int index = campBattles.size() - 1; index >= 0; index--) {
            CampBattle campFightRoom = campBattles.get(index);
            if ((!campFightRoom.needExpel()) && !campFightRoom.isFull(0)) {
                for (int campType = CampManager.CAMPTYPE_QIN; campType <= CampManager.CAMPTYPE_CHU; campType++) {
                    if (!campFightRoom.isFull(campType)) {
                        Iterator<CampFightMatchInfo> iterator = campMatchingMap[campType].values().iterator();
                        while (iterator.hasNext() && !campFightRoom.isFull(campType)) {
                            CampFightMatchInfo campFightMatchInfo = iterator.next();
                            com.stars.util.LogUtil.info("roleId:{} enter room:{}", campFightMatchInfo.getRoleId(), campFightRoom.getBattleId());
                            campFightRoom.addFighter(campFightMatchInfo);
                            roleRoomMap.put(campFightMatchInfo.getRoleId(), campFightRoom.getBattleId());
                            iterator.remove();
                        }
                    }
                }
            }
            if ((campFightRoom.getAllPlayerFightEntity().size() < CampManager.campActivity2MatchNum) && campFightRoom.checkLessWaitingTime()) {
                continue;
            }
            if (campFightRoom.getAllPlayerFightEntity().size() == 0) {
                /**
                 * 无人将不开启战役
                 */
                continue;
            }
            if (!campFightRoom.isFull0(0)) {
                campFightRoom.addCampRobotFighter();
            }
            if (!campFightRoom.isFighting()) {
                /**
                 * 为房间内的角色创建战役，第一次人满时创建，后续战役将一直存在，除非房间被回收
                 */
                campFightRoom.startFight();
            }
        }
        com.stars.util.LogUtil.info("end handle match pool");
    }

    @Override
    public void onFightCreationSuccessed(int fromServerId, int fightServerId, String fightId, boolean isOk, Object args) {
        CampBattle campBattle = campFightRoomMap.get(fightId);
        if (campBattle == null) {
            return;
        }
        if (isOk) {
            com.stars.util.LogUtil.info("camp fight create succeess:{}", fightId);
            List<FighterEntity> allCampFightEntity = campBattle.getAllPlayerFightEntity();
            for (FighterEntity fightEntity : allCampFightEntity) {
                CampFightMatchInfo campFightMatchInfo = campFightingMap.get(fightEntity.getRoleId());
                if (campFightMatchInfo == null) {
                    continue;
                }
                /**
                 * 初期加入的人逻辑与后期加入不同，建房初期加入的人 先切连接 后加入   ，中途加入的人 先加入后切连接
                 */
                campBattle.addBaseFightEntityId(fightEntity.getUniqueId());
                CampRpcHelper.campLocalFightService().matchFinish(campFightMatchInfo.getFromServerId(), fightServerId, campFightMatchInfo.getRoleId());
                modifyConnect(campFightMatchInfo, fightServerId);
            }
        } else {
            com.stars.util.LogUtil.info("camp fight create fail:{}", fightId);
            List<Long> roomRoleIds = campBattle.getRoomRoleIds();
            for (long roleId : roomRoleIds) {
                campBattle.leaveRoom(roleId + "", false);
            }
        }

    }

    /**
     * 切换连接
     *
     * @param campFightMatchInfo
     * @param targetServerId
     */
    public void modifyConnect(CampFightMatchInfo campFightMatchInfo, int targetServerId) {
        com.stars.util.LogUtil.info("roleId:{} modify serverid to {}", campFightMatchInfo.getRoleId(), targetServerId);
        long roleId = campFightMatchInfo.getRoleId();
        /**
         * 修改连接服路由
         */
        ModifyConnectorRoute modifyConnectorRoute = new ModifyConnectorRoute();
        modifyConnectorRoute.setServerId(targetServerId);
        modifyConnectorRoute.setRoleId(roleId);
        CampRpcHelper.roleService().send(campFightMatchInfo.getFromServerId(), roleId, modifyConnectorRoute);
    }

    @Override
    public void modifyConnect(String fightUid, int targetServerId) {
        long roleId = Long.parseLong(fightUid);
        CampFightMatchInfo campFightMatchInfo = campFightingMap.get(roleId);
        modifyConnect(campFightMatchInfo, targetServerId);
    }


    /**
     * 获取更多匹配角色的阵营id
     *
     * @return
     */
    public int getMoreMatchInfoCamp() {
        if (campFightingNum[CampManager.CAMPTYPE_QIN] >= campFightingNum[CampManager.CAMPTYPE_CHU]) {
            return CampManager.CAMPTYPE_QIN;
        }
        return CampManager.CAMPTYPE_CHU;
    }

    /**
     * 申请足够的房间
     */
    public void applyBattle() {
        int moreMatchInfoCamp = getMoreMatchInfoCamp();
        int number = campFightingNum[moreMatchInfoCamp];
        int roomNumber = number / CampBattle.AVALIABLE_MEMBER_COUNT;
        roomNumber = number % CampBattle.AVALIABLE_MEMBER_COUNT == 0 ? roomNumber : roomNumber + 1;
        List<CampBattle> needExpelRooms = new ArrayList<>();
        for (CampBattle campBattle : campFightRoomMap.values()) {
            if (campBattle.needExpel()) {
                needExpelRooms.add(campBattle);
            }
        }
        if (roomNumber > campFightRoomMap.size() - needExpelRooms.size()) {
            com.stars.util.LogUtil.info("room num:{} is less;need destroy room num:{},need  room num:{}", campFightRoomMap.size(), needExpelRooms.size(), roomNumber);
            for (int index = campFightRoomMap.size() - needExpelRooms.size(); index < roomNumber; index++) {
                int fightServer = ServiceHelper.fsManagerService().getFightServer(FSManagerServiceActor.FIGHT_SERVER_LEVEL_COMM);
                CampBattle campFightRoom = new CampBattle(fightServer);
                com.stars.util.LogUtil.info("create room:{}", campFightRoom.getBattleId());
                campFightRoom.setCampServerId(MultiServerHelper.getServerId());
                campFightRoomMap.put(campFightRoom.getBattleId(), campFightRoom);
            }
        }
    }


    @Override
    public void onFighterAddingFailed(int fromServerId, int fightServerId, String fightId, Set<Long> entitySet) {
        com.stars.util.LogUtil.error("camp fight add fighter fail:{},roleid:{}", fightId, entitySet);
        CampBattle campBattle = campFightRoomMap.get(fightId);
        for (long roleId : entitySet) {
            CampFightMatchInfo campFightMatchInfo = campFightingMap.get(roleId);
            campMatchingMap[campFightMatchInfo.getCampType()].put(roleId, campFightMatchInfo);
            roleRoomMap.remove(roleId);
            campBattle.leaveRoom(roleId + "", false);
        }
    }

    @Override
    public void handleFighterQuit(int fromServerId, String fightId, long roleId) {
        com.stars.util.LogUtil.info("role:{} quit room:{}", roleId, fightId);
        CampFightMatchInfo campFightMatchInfo = campFightingMap.get(roleId);
        CampBattle campBattle = campFightRoomMap.get(fightId);
        syncScore2GameServer(campBattle, roleId);
        campBattle.leaveRoom(roleId + "", true);
        if (campFightingMap.containsKey(roleId)) {
            campFightingMap.remove(roleId);
            campFightingNum[campFightMatchInfo.getCampType()] -= 1;
        }
        roleRoomMap.remove(roleId);
        modifyConnect(campFightMatchInfo, campFightMatchInfo.getFromServerId());
        CampRpcHelper.roleService().notice(campFightMatchInfo.getFromServerId(), roleId, new CampFightEvent(CampFightEvent.TYPE_EXIT));
    }

    @Override
    public void handleFightDead(int fromServerId, String fightId, Map<String, String> deadMap) {
        com.stars.util.LogUtil.info("in room:{},deadmap:{}", fightId, deadMap);
        CampBattle campBattle = campFightRoomMap.get(fightId);
        boolean isPlayer = false;
        /**
         * 死亡角色结算
         */
        for (Map.Entry<String, String> entry : deadMap.entrySet()) {
            long deadRoleId = Long.parseLong(entry.getKey());
            long killerId = Long.parseLong(entry.getValue());
            CampFightMatchInfo deadCampFightMatchInfo = campFightingMap.get(deadRoleId);
            CampFightMatchInfo killerFightMatchInfo = campFightingMap.get(killerId);
            if (deadCampFightMatchInfo != null) {
                isPlayer = true;
            }
            /**
             * 击杀者计算积分
             */
            CampFightGrowUP campFightGrowUp = campBattle.getCampFightGrowUp(deadRoleId + "");
            try {
                CampGrade campGradeByJobLevel = CampManager.getCampGradeByLevel(campFightGrowUp.getLevel());
                int score = campGradeByJobLevel.getKillgrade();
                Map<String, Integer> scoreMap = new HashMap<>();
                scoreMap.put(killerId + "", score);
                campBattle.updateFighterScore(scoreMap);
            } catch (Exception e) {
                com.stars.util.LogUtil.error(e.getMessage(), e);
            }

            /**
             * 你击杀了谁
             */
            if (killerFightMatchInfo != null) {
                CampRpcHelper.roleService().send(killerFightMatchInfo.getFromServerId(), killerId, new ClientText("campactivity2_tips_killenemy", campFightGrowUp.getName()));
            }
            if (isPlayer) {
                CampFightGrowUP deadCampFightGrowUp = campBattle.getCampFightGrowUp(deadRoleId + "");
                Map<Integer, Integer> reward = new HashMap<>();
                try {
                    if (deadCampFightMatchInfo.getTakeSingleRewardTime() < CampManager.campActivity2MaxSingleScoreTime) {
                        deadCampFightMatchInfo.addTakeSingleRewardTime();
                        List<CampFightSingleScoreReward> campFightSingleScoreRewards = CampManager.getCampFightSingleScoreRewards(deadCampFightGrowUp.getScore());
                        for (CampFightSingleScoreReward scoreReward : campFightSingleScoreRewards) {
                            com.stars.util.MapUtil.add(reward, scoreReward.getReward());
                        }
                        MapUtil.add(reward, CampManager.campActivity2Defendaward);
                        CampUtils.addExtReward(CampUtils.TYPE_ACTIVITY_REWARD, deadCampFightMatchInfo.getCampType(), reward);
                    }
                    ActivityFinishEvent activityFinishEvent = new ActivityFinishEvent(CampActivity.ACTIVITY_ID_QI_CHU_DA_ZUO_ZHAN, reward);
                    CampRpcHelper.roleService().notice(deadCampFightMatchInfo.getFromServerId(), deadRoleId, activityFinishEvent);
                    syncScore2GameServer(campBattle, deadRoleId);
                } catch (Exception e) {
                    com.stars.util.LogUtil.error(e.getMessage(), e);
                }


                /**
                 * 弹出结算
                 */
                CampFightGrowUP campFightGrowUP = campBattle.getCampFightGrowUp(killerId + "");
                String name = campFightGrowUP.getName();
                ClientCampFightPacket clientCampFightPacket = new ClientCampFightPacket(ClientCampFightPacket.SEND_FIGHT_END);
                clientCampFightPacket.setName(name);
                clientCampFightPacket.setMySocre(deadCampFightGrowUp.getScore());
                clientCampFightPacket.setItemMap(reward);
                CampRpcHelper.roleService().send(deadCampFightMatchInfo.getFromServerId(), deadRoleId, clientCampFightPacket);
            } else {
                campBattle.handleRevive(deadRoleId + "");
            }
            campBattle.reset(deadRoleId + "");
            flushScoreRank(campBattle);
        }
    }

    /**
     * 将房间的战斗积分发往房间中的各个角色
     *
     * @param campBattle
     */
    public void flushScoreRank(CampBattle campBattle) {
        ClientCampFightPacket campGrowRank = campBattle.getCampGrowRankPacket();
        List<Long> roomRoleIds = campBattle.getRoomRoleIds();
        for (Long roleId : roomRoleIds) {
            CampFightMatchInfo campFightMatchInfo = campFightingMap.get(roleId);
            if (campFightMatchInfo == null) {
                continue;
            }
            CampRpcHelper.roleService().send(campFightMatchInfo.getFromServerId(), roleId, campGrowRank);
        }
    }

    /**
     * 同步积分到游戏服
     *
     * @param campBattle
     * @param roleId
     */
    public void syncScore2GameServer(CampBattle campBattle, long roleId) {
        CampFightMatchInfo campFightMatchInfo = campFightingMap.get(roleId);
        if (campBattle == null) {
            return;
        }
        try {
            CampFightGrowUP campFightGrowUp = campBattle.getCampFightGrowUp(roleId + "");
            if (campFightGrowUp != null) {
                com.stars.util.LogUtil.info("role:{} sync score:{} to game server", roleId, campFightGrowUp.getScore());
                CampRpcHelper.campLocalFightService().updateFightScore(campFightMatchInfo.getFromServerId(), roleId, campFightGrowUp.getScore());
            }
        } catch (Exception e) {
            com.stars.util.LogUtil.error("syncScore to gameserver error roleid:" + roleId + "+ in battle:" + campBattle.getBattleId(), e);
        }
    }


    @Override
    public void destroyRoom() {
        List<CampBattle> campBattles = new ArrayList<>(campFightRoomMap.values());
        Collections.sort(campBattles);
        for (CampBattle campBattle : campBattles) {
            if (!campBattle.isFighting() && campBattle.isExpired()) {
                /**
                 * 对于到期，没有开启战斗的房间引导销毁
                 */
                campFightRoomMap.remove(campBattle.getBattleId());
                com.stars.util.LogUtil.info("room:{} has bean destroy", campBattle.getBattleId());
            }
            if (campBattle.needDestroy()) {
                try {
                    for (long roleId : campBattle.getRoomRoleIds()) {
                        com.stars.util.LogUtil.info("room:{} need destroy ;force quit:{}", campBattle.getBattleId(), roleId);
                        handleFighterQuit(0, campBattle.getBattleId(), roleId);
                    }
                } catch (Exception e) {
                    com.stars.util.LogUtil.error(e.getMessage(), e);
                }
                campBattle.destroy();
                campFightRoomMap.remove(campBattle.getBattleId());
                com.stars.util.LogUtil.info("room:{} has bean destroy", campBattle.getBattleId());
            }
        }
    }


    @Override
    public void handleContinueFight(int fromServerId, int fightServerId, String fightId, String fightUid) {
        CampActivityVo campActivityVo = CampManager.campActivityMap.get(CampActivity.ACTIVITY_ID_QI_CHU_DA_ZUO_ZHAN);
        /**
         * 活动开关
         */
        if ((!campActivityVo.isOpen()) || (!campActivityVo.checkOpenTime())) {
            long roleId = Long.parseLong(fightUid);
            CampFightMatchInfo campFightMatchInfo = campFightingMap.get(roleId);
            if (campFightMatchInfo != null) {
                ClientCampFightPacket clientCampFightPacket = new ClientCampFightPacket(ClientCampFightPacket.SEND_ACTIVITY_END);
                CampRpcHelper.roleService().send(campFightMatchInfo.getFromServerId(), roleId, clientCampFightPacket);
            }
            return;
        }
        CampBattle oldCampBattle = campFightRoomMap.get(fightId);
        long roleId = Long.parseLong(fightUid);
        CampFightMatchInfo campFightMatchInfo = campFightingMap.get(roleId);
        CampBattle tartetCamp = oldCampBattle;
        if (oldCampBattle.needExpel()) {
            applyBattle();
            List<CampBattle> campBattles = new ArrayList<>(campFightRoomMap.values());
            Collections.sort(campBattles);
            for (int index = campBattles.size() - 1; index >= 0; index--) {
                CampBattle tmpCamp = campBattles.get(index);
                boolean canNotJoin = (tmpCamp.needExpel()) || tmpCamp.isFull(campFightMatchInfo.getCampType());
                if (!canNotJoin) {
                    tartetCamp = tmpCamp;
                    com.stars.util.LogUtil.info("continue fight switch room from :{} to :{}", oldCampBattle.getBattleId(), tartetCamp.getBattleId());
                    break;
                }
            }
            if (!tartetCamp.getBattleId().equals(oldCampBattle.getBattleId())) {
                if (tartetCamp.getFightServerId() != oldCampBattle.getFightServerId()) {
                    /**
                     * 切换不同战斗服，需要清理原战斗服session信息
                     */
                    oldCampBattle.leaveRoom(fightUid, true);
                } else {
                    oldCampBattle.leaveRoom(fightUid, false);
                }
            }
        }
        tartetCamp.addFighter(campFightMatchInfo);
        if (!tartetCamp.isFighting()) {

            com.stars.util.LogUtil.info("new room not start fight ,but continue fight need start fight and add robot:{}", tartetCamp.getBattleId());
            tartetCamp.addCampRobotFighter();
            tartetCamp.startFight();
        }
        roleRoomMap.put(roleId, tartetCamp.getBattleId());
        com.stars.util.LogUtil.info("roleId:{} continue fight in room:{}", fightUid, tartetCamp.getBattleId());

    }


    @Override
    public void updateFighterExp(int fromServerId, int fightServerId, String fightId, HashMap<String, Integer> expMap) {
        CampBattle campBattle = campFightRoomMap.get(fightId);
        campBattle.updateFighterScore(expMap);
        flushScoreRank(campBattle);
    }

    /**
     * @param battleId
     * @param packet
     * @param excludeFightUid 排除不需要通知的角色
     */
    @Override
    public void NotifyTheRoom(String battleId, Packet packet, String... excludeFightUid) {
        List<String> excluedFightUids = Arrays.asList(excludeFightUid);
        CampBattle campBattle = campFightRoomMap.get(battleId);
        if (campBattle == null) {
            return;
        }
        for (long roleId : campBattle.getRoomRoleIds()) {
            if (!excluedFightUids.contains(roleId + "")) {
                CampFightMatchInfo campFightMatchInfo = campFightingMap.get(roleId);
                if (campFightMatchInfo != null) {
                    CampRpcHelper.roleService().send(campFightMatchInfo.getFromServerId(), roleId, packet);
                }
            }
        }
    }

    @Override
    public void onFighterAddingSucceeded(int fromServerId, int fightServerId, String fightId, Set<Long> entitySet) {
        com.stars.util.LogUtil.info("roleid:{} onFighterAddingSucceeded in room:{}", entitySet, fightId);
        boolean isFirstEnter = false;
        CampBattle campBattle = campFightRoomMap.get(fightId);
        ServerOrder nbBuff = ServerOrders.newAddBuffOrderNoCamp(CampManager.BUFF_ID_NB, 1);
        ArrayList<String> fightUids = new ArrayList<>();
        nbBuff.setUniqueIDs(fightUids);
        ServerOrder speedUpBuff = ServerOrders.newAddBuffOrderNoCamp(CampManager.BUFF_ID_SPEED_UP, 1);
        speedUpBuff.setUniqueIDs(fightUids);
        for (long roleId : entitySet) {
            /**
             * 添加无敌buff
             *
             */
            fightUids.add(roleId + "");
            CampFightMatchInfo campFightMatchInfo = campFightingMap.get(roleId);
            if (campFightMatchInfo == null) {
                return;
            }
            if (!campBattle.isRepeatJoin(roleId + "") && !campBattle.isOrigionEntity(roleId + "")) {
                CampRpcHelper.campLocalFightService().matchFinish(campFightMatchInfo.getFromServerId(), fightServerId, campFightMatchInfo.getRoleId());
                modifyConnect(campFightMatchInfo, fightServerId);
            }
            if (!campBattle.isRepeatJoin(roleId + "")) {
                isFirstEnter = true;
            }
            campBattle.addJoinRoomTimes(roleId + "");
        }
        if (isFirstEnter) {
            nbBuff = ServerOrders.newAddBuffOrderNoCamp(CampManager.BUFF_ID_NB_LONG, 1);
            speedUpBuff = ServerOrders.newAddBuffOrderNoCamp(CampManager.BUFF_ID_SPEED_UP_LONG, 1);
            nbBuff.setUniqueIDs(fightUids);
            speedUpBuff.setUniqueIDs(fightUids);
        }
        ClientUpdatePlayer clientUpdatePlayer = new ClientUpdatePlayer();
        clientUpdatePlayer.addOrder(nbBuff);
        clientUpdatePlayer.addOrder(speedUpBuff);
        byte[] bytes = PacketUtil.packetToBytes(clientUpdatePlayer);
        CampRpcHelper.fightBaseService().addServerOrder(fightServerId, FightConst.T_CAMP_FIGHT, fromServerId, fightId, bytes);
    }

    @Override
    public void flushScoreRank(int fromServerId, int fightServerId, String fightId) {
        CampBattle campBattle = campFightRoomMap.get(fightId);
        flushScoreRank(campBattle);
    }

    @Override
    public void updatePVPRoleNum(int campServerId, int roleNum) {
        CampBattle.AVALIABLE_MEMBER_COUNT = roleNum;
    }

    @Override
    public void notifyRoleCurrentScore(String fightUid, Integer score) {
        com.stars.util.LogUtil.info("notice role:{} score:{}", fightUid, score);
        long roleId = Long.parseLong(fightUid);
        CampFightMatchInfo campFightMatchInfo = campFightingMap.get(roleId);
        if (campFightMatchInfo == null) {
            return;
        }
        ClientCampFightPacket clientCampFightPacket = new ClientCampFightPacket(ClientCampFightPacket.SEND_MY_CURRENT_SCORE);
        clientCampFightPacket.setMySocre(score);
        CampRpcHelper.roleService().send(campFightMatchInfo.getFromServerId(), roleId, clientCampFightPacket);
    }

    @Override
    public void logRoomInfo() {
        com.stars.util.LogUtil.info("matching role qin size:{},chu size:{}", campMatchingMap[CampManager.CAMPTYPE_QIN].size(), campMatchingMap[CampManager.CAMPTYPE_CHU].size());
        com.stars.util.LogUtil.info("fighting role qin size:{},chu size:{}", campFightingNum[CampManager.CAMPTYPE_QIN], campFightingNum[CampManager.CAMPTYPE_CHU]);
        int fightingRoomSize = 0;
        for (CampBattle campFightRoom : campFightRoomMap.values()) {
            com.stars.util.LogUtil.info("roomId:{} create by {} is fighting:{},qin camp player size:{},qin camp robot size:{},chu camp player size:{},robot size:{}", campFightRoom.getBattleId(), DateUtil.formatDateTime(campFightRoom.getCreateTime()), campFightRoom.isFighting(), campFightRoom.getCampEntities(CampManager.CAMPTYPE_QIN).size(), campFightRoom.getRobotCampEntities(CampManager.CAMPTYPE_QIN).size(), campFightRoom.getCampEntities(CampManager.CAMPTYPE_CHU).size(), campFightRoom.getRobotCampEntities(CampManager.CAMPTYPE_CHU).size());
            if (campFightRoom.isFighting()) {
                fightingRoomSize++;
            }
        }
        com.stars.util.LogUtil.info("total room size:{}", campFightRoomMap.size());
        com.stars.util.LogUtil.info("not fighting  room size:{}", campFightRoomMap.size() - fightingRoomSize);
        LogUtil.info("fighting  room size:{}", fightingRoomSize);
    }
}
