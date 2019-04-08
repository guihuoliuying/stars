package com.stars.multiserver.fightutil.campfight;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.stars.core.attr.Attribute;
import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.packet.ClientCampFightPacket;
import com.stars.modules.camp.pojo.CampFightGrowUP;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.offlinepvp.OfflinePvpManager;
import com.stars.modules.pk.packet.ClientUpdatePlayer;
import com.stars.modules.pk.packet.ServerOrder;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFight;
import com.stars.modules.scene.prodata.DynamicBlock;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.camp.CampRpcHelper;
import com.stars.multiserver.camp.pojo.CampFightMatchInfo;
import com.stars.multiserver.fight.FightIdCreator;
import com.stars.multiserver.fight.ServerOrders;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.multiserver.fight.handler.phasespk.PhasesPkFightArgs;
import com.stars.network.PacketUtil;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;
import com.stars.util.RandomUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by huwenjun on 2017/7/21.
 */
public class CampBattle implements Comparable<CampBattle> {
    /**
     * 使用缓存，防止中途有角色离开导致结算出问题
     */
    Cache<String, CampFightGrowUP> campFightGrowUPCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).initialCapacity(10).build();
    private String battleId;
    public static int AVALIABLE_MEMBER_COUNT = 5;
    private Map<String, FighterEntity> fighterEntityMap = new HashMap<>();
    /**
     * 创建时期加入的战斗实体id
     */
    private List<String> baseFighterEntityIdList = new ArrayList<>();
    private Map<String, Integer> fighterTimesMap = new HashMap<>();
    private Map<String, CampFightGrowUP> campFightGrowUPMap = new HashMap<>();
    private Map<String, FighterEntity>[] campEntities = new HashMap[3];
    private Map<String, FighterEntity>[] robotEntities = new HashMap[3];
    private boolean isFighting = false;
    private long createTime;
    private int fightServerId;
    private int campServerId;

    public CampBattle(int fightServerId) {
        this.fightServerId = fightServerId;
        battleId = "camp_fight" + fightServerId + "_" + FightIdCreator.creatUUId();
        campEntities[CampManager.CAMPTYPE_QIN] = new HashMap<>();
        campEntities[CampManager.CAMPTYPE_CHU] = new HashMap<>();
        robotEntities[CampManager.CAMPTYPE_QIN] = new HashMap<>();
        robotEntities[CampManager.CAMPTYPE_CHU] = new HashMap<>();
        createTime = System.currentTimeMillis();
    }

    public List<FighterEntity> getCampEntities(int campType) {
        return new ArrayList<>(campEntities[campType].values());
    }

    public FighterEntity getFighterEntity(String fightUid) {
        FighterEntity fighterEntity = fighterEntityMap.get(fightUid);
        return fighterEntity;
    }

    public void addBaseFightEntityId(String fightUid) {
        baseFighterEntityIdList.add(fightUid);
    }

    public boolean isOrigionEntity(String fightUid) {
        return baseFighterEntityIdList.contains(fightUid);
    }

    public Map<String, FighterEntity> getFighterEntityMap() {
        return fighterEntityMap;
    }

    public List<FighterEntity> getRobotCampEntities(int campType) {
        return new ArrayList(robotEntities[campType].values());
    }


    public boolean isFighting() {
        return isFighting;
    }

    public void setFighting(boolean fighting) {
        isFighting = fighting;
    }

    public boolean checkLessWaitingTime() {
        return (System.currentTimeMillis() - createTime) / 1000 <= CampManager.campActivity2MatchMaxTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getCampFighterCount(int campType) {
        return campEntities[campType].size() + robotEntities[campType].size();
    }

    public boolean needDestroy() {
        long second = (System.currentTimeMillis() - createTime) / 1000;
        boolean expire = second / 60 >= CampManager.campActivity2RoomDestroyTime;
        return isFighting && (getAllPlayerFightEntity().size() == 0 || expire);
    }

    public boolean isExpired() {
        long second = (System.currentTimeMillis() - createTime) / 1000;
        boolean expire = second / 60 >= CampManager.campActivity2RoomExpelTime;
        return expire;
    }

    /**
     * 少于规定人数，需要驱逐，以便房间回收
     * 将到期
     *
     * @return
     */
    public boolean needExpel() {
        long second = (System.currentTimeMillis() - createTime) / 1000;
        boolean expire = second / 60 >= CampManager.campActivity2RoomExpelTime;
        return expire;
    }

    public String getBattleId() {
        return battleId;
    }

    /**
     * 0代表所有阵营
     * 不包括机器人的满员
     *
     * @param campType
     * @return
     */
    public boolean isFull(int campType) {
        if (campType == 0) {
            return campEntities[CampManager.CAMPTYPE_QIN].size() == campEntities[CampManager.CAMPTYPE_CHU].size() && campEntities[CampManager.CAMPTYPE_QIN].size() == AVALIABLE_MEMBER_COUNT;
        } else {
            return campEntities[campType].size() == AVALIABLE_MEMBER_COUNT;
        }
    }

    /**
     * 0代表所有阵营
     * 包括机器人在内的判断是否满员
     *
     * @param campType
     * @return
     */
    public boolean isFull0(int campType) {
        if (campType == 0) {
            return campEntities[CampManager.CAMPTYPE_QIN].size() + robotEntities[CampManager.CAMPTYPE_QIN].size() == campEntities[CampManager.CAMPTYPE_CHU].size() + robotEntities[CampManager.CAMPTYPE_CHU].size() && campEntities[CampManager.CAMPTYPE_QIN].size() + robotEntities[CampManager.CAMPTYPE_QIN].size() == AVALIABLE_MEMBER_COUNT;
        } else {
            return campEntities[campType].size() == AVALIABLE_MEMBER_COUNT;
        }
    }

    public void addCampMember(FighterEntity campFightEntity, CampFightGrowUP campFightGrowUP) {
        fighterEntityMap.put(campFightEntity.getUniqueId(), campFightEntity);
        campFightGrowUPMap.put(campFightEntity.getUniqueId(), campFightGrowUP);
        campFightGrowUPCache.put(campFightEntity.getUniqueId(), campFightGrowUP);
        int myCampType = campFightEntity.getCamp();
        for (int campType = CampManager.CAMPTYPE_QIN; campType <= CampManager.CAMPTYPE_CHU; campType++) {
            if (campFightEntity.getIsRobot()) {
                if (campType == myCampType) {
                    robotEntities[campType].put(campFightEntity.getUniqueId(), campFightEntity);
                }
            } else {
                if (campType == myCampType) {
                    campEntities[campType].put(campFightEntity.getUniqueId(), campFightEntity);
                }
            }
        }

    }

    public void addJoinRoomTimes(String fightUid) {
        Integer times = fighterTimesMap.get(fightUid);
        if (times == null) {
            fighterTimesMap.put(fightUid, 0);
        }
        fighterTimesMap.put(fightUid, fighterTimesMap.get(fightUid) + 1);
    }

    public void addCampRobotFighter() {
        for (int campType = CampManager.CAMPTYPE_QIN; campType <= CampManager.CAMPTYPE_CHU; campType++) {
            if (!isFull0(campType)) {
                List<FighterEntity> fighterEntities = OfflinePvpManager.getRandomRobots(CampBattle.AVALIABLE_MEMBER_COUNT - getCampFighterCount(campType));
                for (FighterEntity fighterEntity : fighterEntities) {
                    fighterEntity.setCamp((byte) campType);
                    CampFightMatchInfo campFightMatchInfo = new CampFightMatchInfo();
                    campFightMatchInfo.setCampFightEntity(fighterEntity);
                    campFightMatchInfo.setCampFightGrowUP(CampFightGrowUP.getNewInstance(fighterEntity));
                    addFighter(campFightMatchInfo);
                }
            }
        }
    }

    /**
     * 不包括机器人在内的所有玩家实体列表
     *
     * @return
     */
    public List<FighterEntity> getAllPlayerFightEntity() {
        List<FighterEntity> campFightEntities = new ArrayList<>();
        for (int campType = CampManager.CAMPTYPE_QIN; campType <= CampManager.CAMPTYPE_CHU; campType++) {
            campFightEntities.addAll(campEntities[campType].values());
        }
        return campFightEntities;
    }

    /**
     * 房间内的所有玩家角色id集合
     *
     * @return
     */
    public List<Long> getRoomRoleIds() {
        List<Long> roleIds = new ArrayList<>();
        for (int campType = CampManager.CAMPTYPE_QIN; campType <= CampManager.CAMPTYPE_CHU; campType++) {
            for (FighterEntity fighterEntity : campEntities[campType].values()) {
                roleIds.add(Long.parseLong(fighterEntity.getUniqueId()));
            }
        }
        return roleIds;
    }

    public int getFightServerId() {
        return fightServerId;
    }

    public void setFightServerId(int fightServerId) {
        this.fightServerId = fightServerId;
    }


    public void startFight() {
        PhasesPkFightArgs phasesPkFightArgs = new PhasesPkFightArgs();
        phasesPkFightArgs.setNumOfFighter(getRoomRoleIds().size());
        Map<Long, FighterEntity> map = new HashMap<>();
        for (Map.Entry<String, FighterEntity> entry : fighterEntityMap.entrySet()) {
            map.put(Long.parseLong(entry.getKey()), entry.getValue());
        }
        phasesPkFightArgs.setEntityMap(map);
        phasesPkFightArgs.setTimeLimitOfInitialPhase(0);
        phasesPkFightArgs.setTimeLimitOfClientPreparationPhase(5000);
        setFighting(true);
        LogUtil.info("room:{} start fight", battleId);
        CampRpcHelper.fightBaseService().createFight(fightServerId, FightConst.T_CAMP_FIGHT, campServerId, battleId, createEnterFightPacket(), phasesPkFightArgs);

    }

    private byte[] createEnterFightPacket() {
        ClientEnterFight enterPacket = new ClientEnterFight();
        enterPacket.setFightType(SceneManager.SCENETYPE_CAMP_FIGHT);
        enterPacket.setStageId(CampManager.STAGE_ID_CAMP_FIGHT);
        StageinfoVo stageVo = SceneManager.getStageVo(CampManager.STAGE_ID_CAMP_FIGHT);
        /* 动态阻挡数据 */
        Map<String, Byte> blockStatus = new HashMap<>();
        for (DynamicBlock dynamicBlock : stageVo.getDynamicBlockMap().values()) {
            blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_CLOSE);
            if (dynamicBlock.getShowSpawnId() == 0) {
                blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_OPEN);
            }
        }
        LogUtil.info("camp fight 动态阻挡数据Actor:{}", blockStatus);
        enterPacket.setBlockMap(stageVo.getDynamicBlockMap());
        enterPacket.addBlockStatusMap(blockStatus);
        return PacketUtil.packetToBytes(enterPacket);
    }


    public void handleRevive(String fighterUid) {
        LogUtil.info("robot revive:{} in room:{}", fighterUid, battleId);
        if (!fighterEntityMap.containsKey(fighterUid)) return;
        FighterEntity entity = fighterEntityMap.get(fighterUid);
        if (entity == null) return;
        CampFightMatchInfo campFightMatchInfo = new CampFightMatchInfo();
        campFightMatchInfo.setCampFightEntity(entity);
        campFightMatchInfo.setCampFightGrowUP(CampFightGrowUP.getNewInstance(entity));
        addFighter(campFightMatchInfo);
    }


    public void setCampServerId(int campServerId) {
        this.campServerId = campServerId;
    }


    /**
     * 添加战斗者
     *
     * @param campFightMatchInfo
     */
    public void addFighter(CampFightMatchInfo campFightMatchInfo) {
        LogUtil.info("camp fight roleId:{} join room :{}", campFightMatchInfo.getRoleId(), battleId);
        FighterEntity campFightEntity = campFightMatchInfo.getCampFightEntity();
        CampFightGrowUP campFightGrowUP = campFightMatchInfo.getCampFightGrowUP();
        campFightEntity.setAttribute(campFightGrowUP.getAttribute());
        campFightEntity.setSkills(new HashMap<Integer, Integer>());
        campFightEntity.setSkillDamageMap(new HashMap<Integer, Integer>());
        campFightEntity.setPosition(CampManager.getRandomPosition4Activity2());
        campFightEntity.setModelId(10004);
        campFightEntity.setCurDeityWeapon(0);
        StringBuilder sb = new StringBuilder();
        if (campFightEntity.getIsRobot()) {
            sb.append("isRobot=1").append(";");
            sb.append("isAuto=1").append(";");
            Attribute robotAttribute = new Attribute();
            /**
             * 机器人削弱属性
             */
            robotAttribute.addAttribute(campFightGrowUP.getAttribute(), CampManager.campActivity2AiAttr, 1000);
            campFightEntity.setAttribute(robotAttribute);
        }
        sb.append("commonOfficerId=").append(campFightGrowUP.getCommonOfficerId()).append(";");
        sb.append("rareOfficerId=").append(campFightGrowUP.getRareOfficerId()).append(";");
        sb.append("designateOfficerId=").append(campFightGrowUP.getDesignateOfficerId()).append(";");
        sb.append("viplevel=").append(campFightGrowUP.getVipLevel());
        campFightEntity.setExtraValue(sb.toString());
        addCampMember(campFightEntity, campFightGrowUP);
        if (isFighting()) {
            if (!isRepeatJoin(campFightEntity.getUniqueId())) {
                /**
                 * 战斗途中添加真人移除机器人
                 */
                if (!campFightEntity.getIsRobot()) {
                    /**
                     * 移除一个机器人
                     */
                    List<String> fightUids = RandomUtil.random(robotEntities[campFightEntity.getCamp()].keySet(), 1);
                    if (fightUids.size() > 0) {
                        leaveRoom(fightUids.get(0), false);
                    }
                }
                /**
                 * 通知房间角色有人加入
                 */
                ClientText clientText = new ClientText("campactivity2_tips_add", campFightEntity.getName());
                ServiceHelper.campRemoteFightService().NotifyTheRoom(battleId, clientText, campFightEntity.getUniqueId());
            }
            List<FighterEntity> fighterEntities = new ArrayList<>();
            fighterEntities.add(campFightEntity);
            CampRpcHelper.fightBaseService().addFighter(fightServerId, FightConst.T_CAMP_FIGHT, campServerId, battleId, fighterEntities);
        }
        if (campFightEntity.getIsRobot()) {
            addJoinRoomTimes(campFightEntity.getUniqueId());
        }
    }

    @Override
    public int compareTo(CampBattle o) {
        int diff = getAllPlayerFightEntity().size() - o.getAllPlayerFightEntity().size();
        if (diff == 0) {
            if (getCreateTime() < o.getCreateTime()) {
                diff = 1;
            } else {
                diff = -1;
            }
        }
        return diff;
    }

    /**
     * 没人的房间销毁掉
     */
    public void destroy() {
        CampRpcHelper.fightBaseService().stopFight(fightServerId, FightConst.T_CAMP_FIGHT, campServerId, battleId);
    }

    /**
     * 离开房间的数据移除工作
     *
     * @param fightUid
     */
    public void leaveRoom(String fightUid, boolean clearServer) {
        LogUtil.info("fight entity:{} leave room:{}", fightUid, battleId);
        FighterEntity fighterEntity = fighterEntityMap.get(fightUid);
        if (fighterEntity == null) {
            return;
        }
        fighterEntityMap.remove(fightUid);
        int campType = fighterEntity.getCamp();
        campEntities[campType].remove(fightUid);
        robotEntities[campType].remove(fightUid);
        campFightGrowUPMap.remove(fightUid);
        fighterTimesMap.remove(fightUid);
        baseFighterEntityIdList.remove(fightUid);
        if (isFighting) {
            List<String> removeFighters = new ArrayList<>();
            removeFighters.add(fightUid);
            ClientUpdatePlayer clientUpdatePlayer = new ClientUpdatePlayer();
            clientUpdatePlayer.setRemoveFighter(removeFighters);
            byte[] bytes = PacketUtil.packetToBytes(clientUpdatePlayer);
            CampRpcHelper.fightBaseService().addServerOrder(fightServerId, FightConst.T_CAMP_FIGHT, campServerId, battleId, bytes);
            /**
             * 通知房间角色有人离开
             */
            ClientText clientText = new ClientText("campactivity2_tips_dropout", fighterEntity.getName());
            ServiceHelper.campRemoteFightService().NotifyTheRoom(battleId, clientText, fightUid);
        }
        if (clearServer) {
            List<Long> roleIds = new ArrayList<>();
            roleIds.add(Long.parseLong(fightUid));
            CampRpcHelper.fightBaseService().removeFromFightActor(fightServerId, FightConst.T_CAMP_FIGHT, campServerId, battleId, roleIds);
        }
    }


    /**
     * 更新战斗者等级经验
     *
     * @param expMap
     */
    public void updateFighterScore(Map<String, Integer> expMap) {
        for (Map.Entry<String, Integer> entry : expMap.entrySet()) {
            CampFightGrowUP campFightGrowUP = getCampFightGrowUp(entry.getKey());
            int oldLevel = campFightGrowUP.getLevel();
            boolean canLevelUp = campFightGrowUP.addScore(entry.getValue());
            ServiceHelper.campRemoteFightService().notifyRoleCurrentScore(entry.getKey(), campFightGrowUP.getScore());
            if (canLevelUp) {
                levelUpNotify(entry.getKey(), oldLevel);
            }
        }
    }

    /**
     * 等级提升通知
     *
     * @param fightUid
     * @param oldLevel
     */
    public void levelUpNotify(String fightUid, int oldLevel) {
        CampFightGrowUP campFightGrowUP = getCampFightGrowUp(fightUid);
        Map<Integer, Integer> buffs = campFightGrowUP.getBuffs();
        /**
         * 修改属性同步
         */
        ClientUpdatePlayer packet = new ClientUpdatePlayer();
        for (Map.Entry<Integer, Integer> entry : buffs.entrySet()) {
            ServerOrder order = ServerOrders.newAddBuffOrderNoCamp(entry.getKey(), entry.getValue());
            ArrayList<String> fightUids = new ArrayList<>();
            fightUids.add(fightUid);
            order.setUniqueIDs(fightUids);
            packet.addOrder(order);
        }
        byte[] bytes = PacketUtil.packetToBytes(packet);
        CampRpcHelper.fightBaseService().addServerOrder(fightServerId, FightConst.T_CAMP_FIGHT,
                MultiServerHelper.getServerId(), battleId, bytes);
        for (int index = oldLevel; index < campFightGrowUP.getLevel(); index++) {
            CampFightGrowUP campFightGrowUPClone = (CampFightGrowUP) campFightGrowUP.clone();
            campFightGrowUPClone.setLevel(index + 1);
            ClientCampFightPacket clientCampFightPacket = new ClientCampFightPacket(ClientCampFightPacket.SEND_LEVEL_UP_NOTIFY);
            clientCampFightPacket.setTheLevelUpGuy(campFightGrowUPClone);
            ServiceHelper.campRemoteFightService().NotifyTheRoom(battleId, clientCampFightPacket);
        }

    }

    public CampFightGrowUP getCampFightGrowUp(String fightUid) {
        CampFightGrowUP campFightGrowUP = campFightGrowUPMap.get(fightUid);
        if (campFightGrowUP == null) {
            LogUtil.info("role:{} leave，so data from cache in room:{}", fightUid, battleId);
            campFightGrowUP = campFightGrowUPCache.getIfPresent(fightUid);
        }
        return campFightGrowUP;
    }


    /**
     * 获取积分排名相关数据包
     *
     * @return
     */
    public ClientCampFightPacket getCampGrowRankPacket() {
        List<CampFightGrowUP> campFightGrowUPs = new ArrayList<>(campFightGrowUPMap.values());
        Collections.sort(campFightGrowUPs);
        List<CampFightGrowUP> ranks = new ArrayList<>();
        int rank = 0;
        for (CampFightGrowUP campFightGrowUP : campFightGrowUPs) {
            rank++;
            ranks.add(campFightGrowUP);
            /**
             * 前五名
             */
            if (rank >= 5) {
                break;
            }
        }
        ClientCampFightPacket clientCampFightPacket = new ClientCampFightPacket(ClientCampFightPacket.SEND_SCORE_RANK);
        clientCampFightPacket.setCampFightGrowUPList(ranks);
        return clientCampFightPacket;
    }

    public boolean isRepeatJoin(String fightUid) {
        Integer times = fighterTimesMap.get(fightUid);
        if (times == null) {
            return false;
        }
        return times > 0;
    }

    public void reset(String key) {
        CampFightGrowUP campFightGrowUP = campFightGrowUPMap.get(key);
        if (campFightGrowUP != null) {
            campFightGrowUP.reset();
        }
    }

    public static void main(String[] args) {
        Attribute attribute = new Attribute();
        attribute.setHp(100);
        attribute.setAttack(1000);
        Attribute robotAttribute = new Attribute();
        /**
         * 机器人削弱属性
         */
        robotAttribute.addAttribute(attribute, 7, 10);
        System.out.println(robotAttribute.getHp());
        System.out.println(robotAttribute.getAttack());
    }
}
