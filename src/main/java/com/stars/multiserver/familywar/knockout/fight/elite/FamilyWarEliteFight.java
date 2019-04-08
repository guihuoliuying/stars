package com.stars.multiserver.familywar.knockout.fight.elite;

import com.stars.core.activityflow.ActivityFlowUtil;
import com.stars.core.attr.Attribute;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.modules.familyactivities.war.packet.*;
import com.stars.modules.pk.packet.ClientUpdatePlayer;
import com.stars.modules.pk.packet.ServerOrder;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFamilyWarEliteFight;
import com.stars.modules.scene.prodata.DynamicBlock;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.prodata.SkillVo;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.FamilyWarRpcHelper;
import com.stars.multiserver.familywar.FamilyWarUtil;
import com.stars.multiserver.familywar.event.FamilyWarSendPacketEvent;
import com.stars.multiserver.familywar.flow.FamilyWarKnockoutFlow;
import com.stars.multiserver.familywar.knockout.FamilyWarKnockout;
import com.stars.multiserver.familywar.knockout.FamilyWarKnockoutBattle;
import com.stars.multiserver.familywar.qualifying.FamilyWarQualifying;
import com.stars.multiserver.fight.ServerOrders;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.network.PacketUtil;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;
import com.stars.services.activities.ActConst;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;
import io.netty.buffer.Unpooled;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import static com.stars.modules.familyactivities.war.FamilyActWarManager.*;
import static com.stars.multiserver.familywar.FamilyWarConst.K_CAMP1;
import static com.stars.multiserver.familywar.FamilyWarConst.K_CAMP2;
import static java.lang.Long.parseLong;

/**
 * Created by zhaowenshuo on 2016/11/15.
 */
public class FamilyWarEliteFight {

    private FamilyWarKnockoutBattle battle;

    /* 战斗服相关 */
    private String fightId;
    private int fightServerId;
    private int fightState;

    private int camp1MainServerId;
    private int camp2MainServerId;
    private long camp1FamilyId;
    private long camp2FamilyId;
    private String camp1FamilyName;
    private String camp2FamilyName;
    private int camp1BuffId;
    private int camp2BuffId;
    private Set<Integer> camp1BuffInstanceId;
    private Set<Integer> camp2BuffInstanceId;

    private long camp1TotalFightScore;
    private long camp2TotalFightScore;

    private Map<String, FighterEntity> camp1FighterMap;
    private Map<String, FighterEntity> camp2FighterMap;
    private Map<String, FighterEntity> fighterMap = new HashMap<>();
    private Map<String, Map<String, Long>> sufferedDamageMap = new HashMap<>(); // (受害者, (攻击者, 伤害值))
    private Map<String, Integer> comboKillCountMap = new HashMap<>(); // (roleId, 连杀数)
    private int invincibleBuffInstId;

    /* 特殊怪的uid */
    private Map<String, EliteFightTower> towerMap = new HashMap<>();

    /* 复活相关（真TM蛋疼） */
    private ConcurrentHashMap<String, Long> reviveMap;
    private Map<String, Integer> payReviveCountMap;
    private Map<String, Integer> reviveStateMap; // uid -- state(1:真人,0:机器人)

    /* 统计数据 */
    private FamilyWarEliteFightStat stat;
    private int battleType;

    /* 内存数据，方便调用 */
    private Map<Long, List<Long>> fighterIdListMap; // (faimlyId, list of fighter id)

    public void createFight(int count, Map<Integer, FamilyWarEliteFightStat> statMap, int camp1MoraleCache, int
            camp2MoraleCache, int battleType) {
        this.battleType = battleType;
        // 初始化
        fighterIdListMap = new HashMap<>();
        List<Long> list = null;
        list = new ArrayList<>();
        for (String roleId : camp1FighterMap.keySet()) {
            list.add(parseLong(roleId));
        }
        fighterIdListMap.put(camp1FamilyId, list);
        list = new ArrayList<>();
        for (String roleId : camp2FighterMap.keySet()) {
            list.add(parseLong(roleId));
        }
        fighterIdListMap.put(camp2FamilyId, list);

        // 准备RPC调用
        FamilyWarEliteFightArgs args = new FamilyWarEliteFightArgs();
        args.setBattleId(battle.getBattleId());
        args.setCamp1MainServerId(camp1MainServerId);
        args.setCamp2MainServerId(camp2MainServerId);
        args.setCreateTimestamp(System.currentTimeMillis());
        Map<Long, Byte> campMap = new HashMap<>();
        Map<Long, Integer> roleWarType = new HashMap<>();
        for (String roleId : camp1FighterMap.keySet()) {
            campMap.put(parseLong(roleId), K_CAMP1);
            roleWarType.put(parseLong(roleId), battleType);
        }
        for (String roleId : camp2FighterMap.keySet()) {
            campMap.put(parseLong(roleId), K_CAMP2);
            roleWarType.put(parseLong(roleId), battleType);
        }
        args.setCampMap(campMap);
        args.setRoleWarType(roleWarType);
        int maxFightSocre = 0;
        for (FighterEntity entity : camp1FighterMap.values()) {
            maxFightSocre = entity.getFightScore() > maxFightSocre ? entity.getFightScore() : maxFightSocre;
        }
        for (FighterEntity entity : camp2FighterMap.values()) {
            maxFightSocre = entity.getFightScore() > maxFightSocre ? entity.getFightScore() : maxFightSocre;
        }
        Map<String, FighterEntity> nonPlayerEntity = FamilyWarUtil.getMonsterFighterEntity(stageIdOfEliteFight);
        for (FighterEntity entity : nonPlayerEntity.values()) {
            Byte towerType = towerTypeMap.get(entity.getSpawnConfigId());
            if (towerType == null) {
                continue;
            }
            printTowerAttr(entity, "计算前|成员最高战力" + maxFightSocre);
            entity.getAttribute().setAttack((int) (entity.getAttribute().getAttack() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_attack));
            entity.getAttribute().setHp((int) (entity.getAttribute().getHp() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_hp));
            entity.getAttribute().setMaxhp((int) (entity.getAttribute().getMaxhp() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_hp));
            entity.getAttribute().setDefense((int) (entity.getAttribute().getDefense() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_defense));
            entity.getAttribute().setHit((int) (entity.getAttribute().getHit() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_hit));
            entity.getAttribute().setAvoid((int) (entity.getAttribute().getAvoid() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_avoid));
            entity.getAttribute().setCrit((int) (entity.getAttribute().getCrit() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_crit));
            entity.getAttribute().setAnticrit((int) (entity.getAttribute().getAnticrit() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_anticrit));
            printTowerAttr(entity, "计算后");
            EliteFightTower tower = new EliteFightTower(entity.getUniqueId(), entity.getCamp(), towerType, entity.getPosition(), entity.getAttribute().getMaxhp());
            towerMap.put(tower.getUid(), tower);
        }

        // 初始化每场统计数据
        stat = new FamilyWarEliteFightStat(camp1FamilyId, camp1FamilyName, camp2FamilyId, camp2FamilyName, camp1MoraleCache, camp2MoraleCache);
        for (FighterEntity entity : fighterMap.values()) {
            long fighterId = parseLong(entity.getUniqueId());
            stat.addPersonalStat(fighterId, entity.getName(), campMap.get(fighterId));
        }
        if (count == 1) {
            stat.setCamp1TotalPoints(FamilyActWarManager.originalPoints);
            stat.setCamp2TotalPoints(FamilyActWarManager.originalPoints);
        }
        statMap.put(count, stat);
        reviveMap = new ConcurrentHashMap<>();
        payReviveCountMap = new ConcurrentHashMap<>();
        camp1BuffInstanceId = new HashSet<>();
        camp2BuffInstanceId = new HashSet<>();
        reviveStateMap = new ConcurrentHashMap<>();

        // RPC调用
        battle.fightService().createFight(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                MultiServerHelper.getServerId(), fightId, createEnterEliteFightPacket(), args);

        battle.fightService().addMonster(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                MultiServerHelper.getServerId(), fightId, new ArrayList<>(nonPlayerEntity.values()));

        List<FighterEntity> entityList = new ArrayList<>();
        StageinfoVo stageVo = SceneManager.getStageVo(FamilyActWarManager.stageIdOfEliteFight);
        for (Entry<String, FighterEntity> entry : fighterMap.entrySet()) {
            if (FamilyWarConst.openAI) {
                entry.getValue().setExtraValue("isAuto=1");
            }
            if (campMap.get(Long.parseLong(entry.getKey())) == K_CAMP1) {
                entry.getValue().setCamp(K_CAMP1);
                entry.getValue().setPosition(stageVo.getPosition());
                entry.getValue().setRotation(stageVo.getRotation());
                LogUtil.info("familywar|camp1,type:{},roleId:{},position:{}", entry.getValue().getFighterType(), entry.getKey(), entry.getValue().getPosition());
            } else {
                entry.getValue().setCamp(K_CAMP2);
                entry.getValue().setPosition(stageVo.getEnemyPos(0));
                entry.getValue().setRotation(stageVo.getEnemyRot(0));
                LogUtil.info("familywar|camp2,type:{},roleId:{},position:{}", entry.getValue().getFighterType(), entry.getKey(), entry.getValue().getPosition());
            }
            reviveStateMap.put(entry.getKey(), FamilyWarConst.AI);
        }
        entityList.addAll(fighterMap.values());
        battle.fightService().addFighterNotSend(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                MultiServerHelper.getServerId(), fightId, new ArrayList<>(entityList));        //通知玩家进入战斗场景
        ClientFamilyWarBattleStartTips packet = new ClientFamilyWarBattleStartTips();
        LogUtil.info("familywar|通知阵营1玩家:{}|进入精英战场", camp1FighterMap.keySet());
        for (String fighterId : camp1FighterMap.keySet()) {
            battle.roleService().notice(camp1MainServerId, parseLong(fighterId), new FamilyWarSendPacketEvent(packet, FamilyWarConst.SCENE_TYPE_SAFE));
        }
        LogUtil.info("familywar|通知阵营2玩家:{}|进入精英战场", camp2FighterMap.keySet());
        for (String fighterId : camp2FighterMap.keySet()) {
            battle.roleService().notice(camp2MainServerId, parseLong(fighterId), new FamilyWarSendPacketEvent(packet, FamilyWarConst.SCENE_TYPE_SAFE));
        }
        //给内塔跟基地加上无敌的buff
        addInvincibleBuff();
    }

    private void printTowerAttr(FighterEntity entity, String text) {
        Attribute attribute = entity.getAttribute();
        LogUtil.info("familywar|塔属性:{}|uid:{},hp:{},attack:{},anticrit:{},avoid:{},crit:{},defense:{},hit:{}"
                , text, entity.getUniqueId(), attribute.getHp(), attribute.getAttack(), attribute.getAnticrit(), attribute.getAvoid(), attribute.getCrit(), attribute.getDefense(), attribute.getHit());
    }

    /**
     * 添加无敌buff
     */
    public void addInvincibleBuff() {
        ArrayList<String> addBuffTowerList = new ArrayList<>();
        for (EliteFightTower tower : towerMap.values()) {
            if (tower.getType() == FamilyWarConst.K_TOWER_TYPE_BASEBOT
                    || tower.getType() == FamilyWarConst.K_TOWER_TYPE_BASETOP
                    || tower.getType() == FamilyWarConst.K_TOWER_TYPE_CRYSTAL) {
                addBuffTowerList.add(tower.getUid());
            }
        }
        ServerOrder order = ServerOrders.newAddBuffOrder(ServerOrder.NONE, ServerOrder.NONE, FamilyActWarManager.invincibleBuffId, 1);
        invincibleBuffInstId = order.getInstanceId();
        order.setUniqueIDs(addBuffTowerList);
        battle.fightService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                MultiServerHelper.getServerId(), fightId, order);
    }

    /**
     * 移除无敌buff
     */
    public void removeInvincibleBuff(byte camp, byte category) {
        ArrayList<String> removeBuffTowerList = new ArrayList<>();
        for (EliteFightTower tower : towerMap.values()) {
            if (tower.getCamp() != camp) continue;
            if (category == FamilyWarConst.K_TOWER_CATEGORY_OUTER) {
                if (tower.getType() == FamilyWarConst.K_TOWER_TYPE_BASETOP || tower.getType() == FamilyWarConst.K_TOWER_TYPE_BASEBOT) {
                    removeBuffTowerList.add(tower.getUid());
                }
            } else if (category == FamilyWarConst.K_TOWER_CATEGORY_INNER) {
                if (tower.getType() == FamilyWarConst.K_TOWER_TYPE_CRYSTAL) {
                    removeBuffTowerList.add(tower.getUid());
                }
            }
        }
        ServerOrder order = ServerOrders.newRemoveBuffOrder(ServerOrder.NONE, invincibleBuffInstId);
        order.setUniqueIDs(removeBuffTowerList);
        battle.fightService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                MultiServerHelper.getServerId(), fightId, order);
    }

    public void stopFight() {
        battle.fightService().stopFight(
                fightServerId,
                FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                MultiServerHelper.getServerId(),
                fightId);
    }

    public void endFight(boolean hasNext) {
        double camp1TowerTotalHp = 0;
        double camp2TowerTotalHp = 0;
        double camp1TowerHp = 0;
        double camp2TowerHp = 0;
        for (EliteFightTower tower : towerMap.values()) {
            if (tower.getCamp() == K_CAMP1) {
                camp1TowerTotalHp += tower.getMaxHp();
                camp1TowerHp += tower.getHp();
            } else if (tower.getCamp() == K_CAMP2) {
                camp2TowerTotalHp += tower.getMaxHp();
                camp2TowerHp += tower.getHp();
            }
        }
        double camp1Tower = camp1TowerHp / camp1TowerTotalHp;
        double camp2Tower = camp2TowerHp / camp2TowerTotalHp;
        LogUtil.info("familywar|elitefight结算时塔的血量 camp1Tower :{} ,camp1TowerHp:{} ,camp1TowerTotalHp:{}, camp2Tower:{}, camp2TowerHp:{}, camp2TowerTotalHp:{}",
                camp1Tower, camp1TowerHp, camp1TowerTotalHp, camp2Tower, camp2TowerHp, camp2TowerTotalHp);
        stat.setCamp1TowerHp(camp1Tower);
        stat.setCamp2TowerHp(camp2Tower);
        stat.setCamp1FightScore(camp1TotalFightScore);
        stat.setCamp2FightScore(camp2TotalFightScore);
        if (camp1TowerHp != camp2TowerHp) {
            long winnerFamilyId = stat.getCamp1TowerHp() > stat.getCamp2TowerHp() ? camp1FamilyId : camp2FamilyId;
            long loserFamilyId = stat.getCamp1TowerHp() > stat.getCamp2TowerHp() ? camp2FamilyId : camp1FamilyId;
            battle.finishEliteFight(fightId, winnerFamilyId, loserFamilyId, stat, hasNext, FamilyWarConst.WIN_BY_TOWER_HP);
            return;
        }
        if (stat.getCamp1Morale() != stat.getCamp2Morale()) {
            long winnerFamilyId = stat.getCamp1Morale() > stat.getCamp2Morale() ? camp1FamilyId : camp2FamilyId;
            long loserFamilyId = stat.getCamp1Morale() > stat.getCamp2Morale() ? camp2FamilyId : camp1FamilyId;
            battle.finishEliteFight(fightId, winnerFamilyId, loserFamilyId, stat, hasNext, FamilyWarConst.WIN_BY_CAMP_MORALE);
            return;
        }
        if (camp1TotalFightScore != camp2TotalFightScore) {
            long winnerFamilyId = camp1TotalFightScore > camp2TotalFightScore ? camp1FamilyId : camp2FamilyId;
            long loserFamilyId = camp1TotalFightScore > camp2TotalFightScore ? camp2FamilyId : camp1FamilyId;
            battle.finishEliteFight(fightId, winnerFamilyId, loserFamilyId, stat, hasNext, FamilyWarConst.WIN_BY_CAMP_FIGHTSCORE);
            return;
        }
        battle.finishEliteFight(fightId, camp1FamilyId, camp2FamilyId, stat, hasNext, FamilyWarConst.WIN_BY_RANDOM);
    }

    public void endAllFight() {
        battle.finishFightResult(camp1TotalFightScore, camp2TotalFightScore);
    }

    private byte[] createEnterEliteFightPacket() {
        StageinfoVo stageVo = SceneManager.getStageVo(stageIdOfEliteFight);
        ClientEnterFamilyWarEliteFight enterPacket = new ClientEnterFamilyWarEliteFight();
        enterPacket.setFightType(SceneManager.SCENETYPE_FAMILY_WAR_ELITE_FIGHT); // buffer
        enterPacket.setStageId(stageIdOfEliteFight);
        enterPacket.setLimitTime(FamilyActWarManager.familywar_lasttime);
        enterPacket.setStartRemainderTime(FamilyActWarManager.DYNAMIC_BLOCK_TIME);
        enterPacket.setSkillVoMap(new HashMap<Integer, SkillVo>(SkillManager.getSkillVoMap()));
        enterPacket.addMonsterVoMap(stageVo.getMonsterVoMap());
        /* 动态阻挡数据 */
        Map<String, Byte> blockStatus = new HashMap<>();
        for (DynamicBlock dynamicBlock : stageVo.getDynamicBlockMap().values()) {
            blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_CLOSE);
            if (dynamicBlock.getShowSpawnId() == 0) {
                blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_OPEN);
            }
        }
        LogUtil.info("动态阻挡数据Elites:{}", blockStatus);
        enterPacket.setBlockMap(stageVo.getDynamicBlockMap());
        enterPacket.addBlockStatusMap(blockStatus);
        return PacketUtil.packetToBytes(enterPacket);
    }

    /**
     * 离比赛结束的剩余时间
     *
     * @return
     */
    public int getBattleEndRemainderTime() {
        if (FamilyWarConst.STEP_OF_SUB_FLOW != FamilyWarKnockoutFlow.STEP_START_QUARTER_FINALS
                && FamilyWarConst.STEP_OF_SUB_FLOW != FamilyWarKnockoutFlow.STEP_START_SEMI_FINALS
                && FamilyWarConst.STEP_OF_SUB_FLOW != FamilyWarKnockoutFlow.STEP_START_FINALS) {
            return 0;
        }
        int tempStep = FamilyWarConst.STEP_OF_SUB_FLOW + 1;
        Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_LOCAL);
        if (StringUtil.isEmpty(flowMap) || !flowMap.containsKey(tempStep)) {
            return 0;
        }
        long endTime = ActivityFlowUtil.getTimeInMillisByCronExpr(flowMap.get(tempStep));
        return (int) ((endTime - System.currentTimeMillis()) / 1000);
    }



    public void onFightCreationSucceeded() {

    }

    public void onFightCreationFailed() {

    }

    public void enter(int mainServerId, long familyId, long roleId, FighterEntity entity) {
        byte camp = (byte) (familyId == camp1FamilyId ? 1 : 2);
        String fighterId = Long.toString(roleId);
        FighterEntity myEntity = fighterMap.put(fighterId, entity);
        myEntity.setState((byte) 0);//每次进入都设置没复活过
        sendStatPacket(familyId, roleId, fighterId);
        if (battle.getFamilyWar() instanceof FamilyWarKnockout) {
            ServiceHelper.familyWarLocalService().onPremittedToEnter(mainServerId, fightServerId, fightId, camp, roleId);
        } else if (battle.getFamilyWar() instanceof FamilyWarQualifying) {
            ServiceHelper.familyWarQualifyingService().onPremittedToEnter(mainServerId, fightServerId, fightId, camp, roleId);
        } else {
            ServiceHelper.familyWarRemoteService().onPremittedToEnter(mainServerId, fightServerId, fightId, camp, roleId);
        }
        LogUtil.info("familywar|玩家状态:{}", reviveStateMap.get(fighterId));
    }

    private void sendStatPacket(long familyId, long roleId, String fighterId) {
        Map<Integer, FamilyWarEliteFightStat> statMap = battle.getStatEliteList();
        ClientFamilyWarBattleStat packet = new ClientFamilyWarBattleStat();
        FamilyWarEliteFightPersonalStat personalStat = stat.getPersonalStatMap().get(roleId);
        packet.setMyKillCount(personalStat.getKillCount());
        packet.setMyDeadCount(personalStat.getDeadCount());
        packet.setMyAssistCount(personalStat.getAssistCount());
        ClientFamilyWarBattleFamilyPoints points = new ClientFamilyWarBattleFamilyPoints();
        for (FamilyWarEliteFightStat fightStat : statMap.values()) {
            if (familyId == camp1FamilyId) {
                points.addMyFamilyPoints(fightStat.getCamp1TotalPoints());
                points.addEnemyFamilyPoints(fightStat.getCamp2TotalPoints());
            } else if (familyId == camp2FamilyId) {
                points.addMyFamilyPoints(fightStat.getCamp2TotalPoints());
                points.addEnemyFamilyPoints(fightStat.getCamp1TotalPoints());

            }
        }
        syncPersonalPoints(Long.toString(roleId), statMap);
        battle.roleService().send(getMainServerId(fighterId), roleId, packet);
        battle.roleService().send(getMainServerId(fighterId), roleId, points);
    }

    public void onFighterAddingSucceeded() {
        // 切换连接
//        MultiServerHelper.modifyConnectorRoute(0L, fightServerId);
    }

    public void onFighterAddingFailed() {

    }

    public void handleDamage(Map<String, HashMap<String, Integer>> damageMap) {
        for (Entry<String, HashMap<String, Integer>> entry : damageMap.entrySet()) {
            String victimId = entry.getKey();
            Map<String, Integer> victimSufferedDamageMap = entry.getValue();
            if (camp1FighterMap.containsKey(victimId) || camp2FighterMap.containsKey(victimId)) {
                if (!sufferedDamageMap.containsKey(victimId)) {
                    sufferedDamageMap.put(victimId, toStringLongMap(victimSufferedDamageMap));
                } else {
                    MapUtil.add(sufferedDamageMap.get(victimId), toStringLongMap(victimSufferedDamageMap));
                }
            }
            if (towerMap.containsKey(victimId)) {
                EliteFightTower tower = towerMap.get(victimId);
                if (tower != null) {
                    for (int demage : victimSufferedDamageMap.values()) {
                        tower.reduceHp(demage);
                    }
                }
            }
        }
    }

    private Map<String, Long> toStringLongMap(Map<String, Integer> map) {
        Map<String, Long> newMap = new HashMap<>();
        for (Entry<String, Integer> entry : map.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue().longValue());
        }
        return newMap;
    }

    public void handleDead(Map<String, String> deadMap) {
        for (Entry<String, String> dead : deadMap.entrySet()) {
            String victimUid = dead.getKey();
            String attackerUid = dead.getValue();

            if (towerMap.containsKey(victimUid)) {
                EliteFightTower tower = towerMap.get(victimUid);
                if (tower.getType() == FamilyWarConst.K_TOWER_TYPE_CRYSTAL) {
                    handleCrystalDead(tower, attackerUid);
                } else {
                    handleTowerDead(tower, attackerUid);
                }
            } else { // 玩家
                handleFighterDead(victimUid, attackerUid);
            }
        }
    }

    private void handleCrystalDead(EliteFightTower tower, String attackerUid) {
        // 检查状态
        // 判断输赢
        long winnerFamilyId = getFamilyId(attackerUid);
        long loserFamilyId = winnerFamilyId == camp1FamilyId ? camp2FamilyId : camp1FamilyId;
//        battle.removeBattle();
        battle.end(fightId, winnerFamilyId, loserFamilyId, stat, true);
        // 结束对战

        // 切换连接

    }

    private void handleTowerDead(EliteFightTower tower, String attackerUid) {
        // 检查状态
        // 强制将血量改成0
        tower.setHp(0);
        // 加积分，加士气
        battle.updateMorale(getFamilyId(attackerUid), moraleDeltaOfDestoryTower); // 己方增加士气
        battle.updateMorale(getOpponentFamilyId(getFamilyId(attackerUid)), -moraleDeltaOfLosingTower); // 对方减少士气
        battle.updateElitePoints(attackerUid, pointsDeltaOfDestoryTower); // 积分
        stat.updatePersonalStat(battle, getMainServerId(attackerUid), parseLong(attackerUid), 0, 0, 0, 0, pointsDeltaOfDestoryTower, reviveStateMap.get(attackerUid) == FamilyWarConst.play);
        syncPersonalPoints(attackerUid, battle.getStatEliteList());
        // 强制同步一次信息
        battle.sendBattleFightUpdateInfo(fightId);

        byte camp = tower.getCamp();
        byte category = FamilyWarConst.K_TOWER_CATEGORY_OUTER;
        if (tower.getType() == FamilyWarConst.K_TOWER_TYPE_TOP
                || tower.getType() == FamilyWarConst.K_TOWER_TYPE_MID
                || tower.getType() == FamilyWarConst.K_TOWER_TYPE_BOT) {
            category = FamilyWarConst.K_TOWER_CATEGORY_OUTER;
        } else if (tower.getType() == FamilyWarConst.K_TOWER_TYPE_BASEBOT
                || tower.getType() == FamilyWarConst.K_TOWER_TYPE_BASETOP) {
            category = FamilyWarConst.K_TOWER_CATEGORY_INNER;
        }
        removeInvincibleBuff(camp, category);
    }

    // FIXME: 2016/12/12 跨服发送数据时需要捕获异常
    private void handleFighterDead(String victimUid, String attackerUid) {
        // 人杀人
        if (fighterMap.containsKey(attackerUid) && fighterMap.containsKey(victimUid)) {
            FighterEntity victimEntity = fighterMap.get(victimUid);
            FighterEntity attackerEntity = fighterMap.get(attackerUid);
            // 提示
            if (reviveStateMap.get(attackerUid) == FamilyWarConst.play) {
                battle.roleService().warn(getMainServerId(attackerUid), parseLong(attackerUid),
                        attackerEntity.getName() + "杀死" + victimEntity.getName());
            }

            // 更新攻击者的连杀数量, 并发送公告
            Integer attackerComboKillCount = comboKillCountMap.get(attackerUid);
            if (attackerComboKillCount == null) {
                comboKillCountMap.put(attackerUid, attackerComboKillCount = 1);
            } else {
                comboKillCountMap.put(attackerUid, attackerComboKillCount += 1);
            }
            if (attackerComboKillCount >= killCountThresholdOfNotice) {
                long attackerFamilyId = getFamilyId(attackerUid);
                ClientText packet = new ClientText("familywar_tips_selfdoublekill", attackerEntity.getName(), attackerComboKillCount.toString());
                for (long roleId : fighterIdListMap.get(attackerFamilyId)) {
                    if (reviveStateMap.get(Long.toString(roleId)) == FamilyWarConst.play) {
                        battle.roleService().notice(getMainServerId(attackerUid), roleId, new FamilyWarSendPacketEvent(packet, FamilyWarConst.SCENE_TYPE_FAMILY_WAR));
                    }
                }

                // 对方
                long victimFamilyId = getFamilyId(victimUid);
                packet = new ClientText("familywar_tips_enemydoublekill", attackerEntity.getName(), attackerComboKillCount.toString());
                for (long roleId : fighterIdListMap.get(victimFamilyId)) {
                    if (reviveStateMap.get(Long.toString(roleId)) == FamilyWarConst.play) {
                        battle.roleService().notice(getMainServerId(victimUid), roleId, new FamilyWarSendPacketEvent(packet, FamilyWarConst.SCENE_TYPE_FAMILY_WAR));
                    }
                }
            }
            // 更新受害者的连杀数量(置为0)，并发送公告
            Integer victimComboKillCount = comboKillCountMap.get(victimUid);
            if (victimComboKillCount != null && victimComboKillCount >= killCountThresholdOfNotice) {
                // 己方
                long attackerFamilyId = getFamilyId(attackerUid);
                ClientText packet = new ClientText("familywar_tips_selfoverdoublekill", attackerEntity.getName(), victimEntity.getName(), victimComboKillCount.toString());
                for (long roleId : fighterIdListMap.get(attackerFamilyId)) {
                    if (reviveStateMap.get(Long.toString(roleId)) == FamilyWarConst.play) {
                        battle.roleService().notice(getMainServerId(attackerUid), roleId, new FamilyWarSendPacketEvent(packet, FamilyWarConst.SCENE_TYPE_FAMILY_WAR));
                    }
                }

                // 对方
                packet = new ClientText("familywar_tips_enemyoverdoublekill", attackerEntity.getName(), victimEntity.getName(), victimComboKillCount.toString());
                long victimFamilyId = getFamilyId(victimUid);
                for (long roleId : fighterIdListMap.get(victimFamilyId)) {
                    if (reviveStateMap.get(Long.toString(roleId)) == FamilyWarConst.play) {
                        battle.roleService().notice(getMainServerId(victimUid), roleId, new FamilyWarSendPacketEvent(packet, FamilyWarConst.SCENE_TYPE_FAMILY_WAR));
                    }
                }
            }
            comboKillCountMap.put(victimUid, 0);

            // 增加士气
            battle.updateMorale(getFamilyId(attackerUid), moraleDeltaOfKillFighterInEliteFight);

            // 计算积分
            Map<String, Long> damageMap = sufferedDamageMap.get(victimUid);
            if (damageMap != null) {
                long totalDamage = MapUtil.sum(damageMap, 0L);
                for (Entry<String, Long> entry : damageMap.entrySet()) {
                    if (towerMap.containsKey(entry.getKey())) continue;
                    double ratio = (entry.getValue() * 1.0) / totalDamage;
                    if (ratio >= damageRatioThresholdOfPersonalPoints) {
                        long pointsDelta = (long) (ratio * coefficientA + coefficientAA);
                        battle.updateElitePoints(entry.getKey(), pointsDelta);
                        int assistDelta = entry.getKey().equals(attackerUid) ? 0 : 1;
                        stat.updatePersonalStat(battle, getMainServerId(entry.getKey()), parseLong(entry.getKey()),
                                0, 0, assistDelta, 0, pointsDelta, reviveStateMap.get(entry.getKey()) == FamilyWarConst.play);
                        syncPersonalPoints(entry.getKey(), battle.getStatEliteList());
                    }
                }
                damageMap.clear();
            }

            // 更新统计信息
            stat.updatePersonalStat(battle, getMainServerId(attackerUid), parseLong(attackerUid), 1, 0, 0, attackerComboKillCount, 0, reviveStateMap.get(attackerUid) == FamilyWarConst.play);
            stat.updatePersonalStat(battle, getMainServerId(victimUid), parseLong(victimUid), 0, 1, 0, 0, 0, reviveStateMap.get(victimUid) == FamilyWarConst.play);
            // 发送双方的连杀包
            if (reviveStateMap.get(attackerUid) == FamilyWarConst.play) {
                battle.roleService().send(getMainServerId(attackerUid),
                        parseLong(attackerUid), new ClientFamilyWarBattleFightKillCount(attackerComboKillCount));
            }
            if (reviveStateMap.get(victimUid) == FamilyWarConst.play) {
                battle.roleService().send(getMainServerId(victimUid),
                        parseLong(victimUid), new ClientFamilyWarBattleFightKillCount(0));
            }

        }
        //搭打人
        if (towerMap.containsKey(attackerUid) && fighterMap.containsKey(victimUid)) {
            //TODO
        }
        reviveMap.put(victimUid, System.currentTimeMillis());
        if (!battle.getFightingInBattleRoleIdMap().contains(Long.parseLong(victimUid))) {
            return;
        }
        // 发送复活框
        Integer payReviveCount = payReviveCountMap.get(victimUid);
        ClientFamilyWarBattleFightRevive packet = new ClientFamilyWarBattleFightRevive();
        packet.setType(ClientFamilyWarBattleFightRevive.TYPE_OF_COUNT);
        packet.setReviveCount(payReviveCount == null ? 0 : payReviveCount.intValue());
        if (reviveStateMap.get(victimUid) == FamilyWarConst.play) {
            battle.roleService().send(getMainServerId(victimUid), Long.parseLong(victimUid), packet);
        }
    }

    public void syncPersonalPoints(String fighterUid, Map<Integer,? extends FamilyWarEliteFightStat> statMap) {
        long roleId = Long.parseLong(fighterUid);
        long rolePoints = 0;
        for (FamilyWarEliteFightStat fightStat : statMap.values()) {
            rolePoints += fightStat.getPersonalStatMap().get(roleId).getPoints();
        }
        if (reviveStateMap.get(Long.toString(roleId)) == FamilyWarConst.play) {
            battle.roleService().send(getMainServerId(fighterUid), roleId, new ClientFamilyWarBattleFightPersonalPoint(rolePoints));
        }
    }

    /**
     * 复活处理
     *
     * @param fighterUid
     * @param reqType
     */
    public void revive(String fighterUid, byte reqType) {
        if (!reviveMap.containsKey(fighterUid)) return;
        FighterEntity entity = fighterMap.get(fighterUid);
        if (entity == null) return;

//    	long now = System.currentTimeMillis();
        entity.setFighterType(FighterEntity.TYPE_PLAYER);
        entity.setState((byte) 1);
        byte camp = (byte) (camp1FighterMap.containsKey(fighterUid) ? 1 : 2);
        entity.setCamp(camp);
        if (reqType == homeRevive) {//安全复活
            StageinfoVo stageVo = SceneManager.getStageVo(FamilyActWarManager.stageIdOfEliteFight);
//    		if (reviveMap.get(fighterUid) + homeReviveTime * 1000 > now) return;
            if (camp == FamilyWarConst.K_CAMP1) { //设置出生点复活
                entity.setPosition(stageVo.getPosition());
                entity.setRotation(stageVo.getRotation());
            } else {
                entity.setPosition(stageVo.getEnemyPos(0));
                entity.setRotation(stageVo.getEnemyRot(0));
            }
            handleRevive(fighterUid);
            reviveMap.remove(fighterUid);
        } else if (reqType == payRevive) {//原地复活（付费）
//    		if (reviveMap.get(fighterUid) + payReviveTime * 1000 >= now) return;
//			Integer payReviveCount = payReviveCountMap.get(fighterUid);
//			if (payReviveCount == null || payReviveCount.intValue() < totalPayRevive) {
//				ServiceHelper.roleService().notice(getMainServerId(fighterUid), parseLong(fighterUid), new FamilyWarRevivePayReqEvent(battle.getBattleId(), fightId, fighterUid));
//			}
//    			else {
//    				ServiceHelper.roleService().warn(getMainServerId(fighterUid), parseLong(fighterUid), "已经不能原地复活了");
//    			}
        }
    }

    public void changePlayerState(long roleId, int state) {
        reviveStateMap.put(Long.toString(roleId), state);
        if (state == FamilyWarConst.play && reviveMap.containsKey(Long.toString(roleId))) {
            LogUtil.info("familywar|如果死掉就先复活");
            revive(Long.toString(roleId), homeRevive);
        }
    }

    /**
     * 处理玩家不能复活的异常
     * 如：玩家濒死状态在敌方塔攻击范围内回城，10秒内再进战场就空血不动了
     */
    public void repairRevive() {
        if (StringUtil.isEmpty(reviveMap)) return;
        try {
            ConcurrentHashMap<String, Long> tmpReviveMap = new ConcurrentHashMap<>(reviveMap);
            long now = System.currentTimeMillis();
            for (Entry<String, Long> entry : tmpReviveMap.entrySet()) {
                if (now - entry.getValue() > (homeReviveTime + 2) * 1000) {
                    revive(entry.getKey(), homeRevive);
                }
            }
        } catch (Exception e) {
            LogUtil.error("familywar|家族战复活异常:" + e.getMessage());
        }
    }

    /**
     * 处理玩家复活
     *
     * @param
     */
    public synchronized void handleRevive(String fighterUid) {
        if (!reviveMap.containsKey(fighterUid)) return;
        FighterEntity entity = fighterMap.get(fighterUid);
        if (entity == null) return;
        List<FighterEntity> entityList = new ArrayList<>();
        ArrayList<String> reviveList = new ArrayList<>();
        entityList.add(entity);
        reviveList.add(entity.uniqueId);
        battle.fightService().addFighter(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                MultiServerHelper.getServerId(), fightId, entityList);
        //发送满血满状态指令
        ServerOrder serverOrder = new ServerOrder();
        serverOrder.setOrderType(ServerOrder.ORDER_TYPE_RESET_CHARACS);
        serverOrder.setUniqueIDs(reviveList);
        sendServerOrder(fightId, serverOrder);//发送服务端lua命令
    }

    /**
     * 发送服务端lua命令
     */
    private void sendServerOrder(String fightId, ServerOrder serverOrder) {
        ClientUpdatePlayer packet = new ClientUpdatePlayer();
        packet.addOrder(serverOrder);
        byte[] bytes = packetToBytes(packet);
        if (battleType == FamilyWarConst.W_TYPE_LOCAL) {
            MainRpcHelper.fightBaseService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                    MultiServerHelper.getServerId(), fightId, bytes);
        } else if (battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
            FamilyWarRpcHelper.fightBaseService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                    MultiServerHelper.getServerId(), fightId, bytes);
        } else if (battleType == FamilyWarConst.W_TYPE_REMOTE) {
            FamilyWarRpcHelper.fightBaseService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                    MultiServerHelper.getServerId(), fightId, bytes);
        }
    }

    /**
     * 将包转为byte[]
     */
    private byte[] packetToBytes(Packet packet) {
        NewByteBuffer buffer = new NewByteBuffer(Unpooled.buffer());
        packet.writeToBuffer(buffer);
        byte[] bytes = new byte[buffer.getBuff().readableBytes()];
        buffer.getBuff().readBytes(bytes);
        buffer.getBuff().release();
        return bytes;
    }

    /**
     * 获得角色复活状态
     *
     * @param fighterUid
     * @return
     */
    public byte getFighterReviveState(String fighterUid) {
        FighterEntity entity = fighterMap.get(fighterUid);
        if (entity == null) return 0;
        return entity.getState();
    }

    /**
     * 添加buff
     *
     * @param familyId
     * @param buffId
     * @param buffLevel
     */
    public final void setBuff(long familyId, int buffId, int debuffId, int buffLevel) {
        LogUtil.info("familywar|buff|familyId:{},buffId:{}debuffId:{},buffLevel:{}",
                familyId, buffId, debuffId, buffLevel);
        if (familyId == camp1FamilyId) {
            removeBuffOrder(camp1BuffInstanceId, K_CAMP1);
            removeBuffOrder(camp2BuffInstanceId, K_CAMP2);
            camp1BuffId = buffId;
            if (buffId != 0) {
                ServerOrder order = ServerOrders.newAddBuffOrder(K_CAMP1, ServerOrder.CHARACTER_TYPE_PLAYER, buffId, buffLevel);
                camp1BuffInstanceId.add(order.getInstanceId());
                battle.fightService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                        MultiServerHelper.getServerId(), fightId, order);
            }
            if (debuffId != 0) {
                ServerOrder order2 = ServerOrders.newAddBuffOrder(K_CAMP2, ServerOrder.CHARACTER_TYPE_MONSTER, debuffId, buffLevel);
                camp2BuffInstanceId.add(order2.getInstanceId());
                battle.fightService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                        MultiServerHelper.getServerId(), fightId, order2);
            }
        } else if (familyId == camp2FamilyId) {
            removeBuffOrder(camp1BuffInstanceId, K_CAMP1);
            removeBuffOrder(camp2BuffInstanceId, K_CAMP2);
            camp2BuffId = buffId;
            if (buffId != 0) {
                ServerOrder order = ServerOrders.newAddBuffOrder(K_CAMP2, ServerOrder.CHARACTER_TYPE_PLAYER, buffId, buffLevel);
                camp2BuffInstanceId.add(order.getInstanceId());
                battle.fightService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                        MultiServerHelper.getServerId(), fightId, order);
            }
            if (debuffId != 0) {
                ServerOrder order2 = ServerOrders.newAddBuffOrder(K_CAMP1, ServerOrder.CHARACTER_TYPE_MONSTER, debuffId, buffLevel);
                camp1BuffInstanceId.add(order2.getInstanceId());
                battle.fightService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                        MultiServerHelper.getServerId(), fightId, order2);
            }
        }
    }

    private void removeBuffOrder(Set<Integer> buffInstanceId, byte camp) {
        if (!buffInstanceId.isEmpty()) {
            for (int buffInstId : buffInstanceId) {
                battle.fightService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                        MultiServerHelper.getServerId(), fightId,
                        ServerOrders.newRemoveBuffOrder(camp, buffInstId));
            }
        }
    }

    public void updateFamilyPoint(long familyId, long points) {
        stat.updateFamilyPoints(familyId, points);
    }

    /**
     * 更新士气
     *
     * @param familyId
     * @param moraleDelta
     */
    public void updateStatMorale(long familyId, int moraleDelta) {
        if (familyId == camp1FamilyId) {
            int preMorale = stat.getCamp1Morale();
            stat.updateMorale(K_CAMP1, moraleDelta);
            battle.sendUpdateBuffInfo(familyId, preMorale, stat.getCamp1Morale());
        } else if (familyId == camp2FamilyId) {
            int preMorale = stat.getCamp2Morale();
            stat.updateMorale(K_CAMP2, moraleDelta);
            battle.sendUpdateBuffInfo(familyId, preMorale, stat.getCamp2Morale());
        }
    }

    public int getMainServerId(String fighterUid) {
        if (camp1FighterMap.containsKey(fighterUid)) {
            return camp1MainServerId;
        }
        if (camp2FighterMap.containsKey(fighterUid)) {
            return camp2MainServerId;
        }
        return 0;
    }

    public long getFamilyId(String fighterUid) {
        if (camp1FighterMap.containsKey(fighterUid)) {
            return camp1FamilyId;
        }
        if (camp2FighterMap.containsKey(fighterUid)) {
            return camp2FamilyId;
        }
        return 0;
    }

    public long getOpponentFamilyId(long familyId) {
        if (familyId == camp1FamilyId) {
            return camp2FamilyId;
        }
        return camp1FamilyId;
    }

    public FamilyWarKnockoutBattle getBattle() {
        return battle;
    }

    public void setBattle(FamilyWarKnockoutBattle battle) {
        this.battle = battle;
    }

    public String getFightId() {
        return fightId;
    }

    public void setFightId(String fightId) {
        this.fightId = fightId;
    }

    public int getFightServerId() {
        return fightServerId;
    }

    public void setFightServerId(int fightServerId) {
        this.fightServerId = fightServerId;
    }

    public int getFightState() {
        return fightState;
    }

    public void setFightState(int fightState) {
        this.fightState = fightState;
    }

    public int getCamp1BuffId() {
        return camp1BuffId;
    }

    public void setCamp1BuffId(int camp1BuffId) {
        this.camp1BuffId = camp1BuffId;
    }

    public int getCamp2BuffId() {
        return camp2BuffId;
    }

    public void setCamp2BuffId(int camp2BuffId) {
        this.camp2BuffId = camp2BuffId;
    }

    public Set<Integer> getCamp1BuffInstanceId() {
        return camp1BuffInstanceId;
    }

    public void setCamp1BuffInstanceId(Set<Integer> camp1BuffInstanceId) {
        this.camp1BuffInstanceId = camp1BuffInstanceId;
    }

    public Set<Integer> getCamp2BuffInstanceId() {
        return camp2BuffInstanceId;
    }

    public void setCamp2BuffInstanceId(Set<Integer> camp2BuffInstanceId) {
        this.camp2BuffInstanceId = camp2BuffInstanceId;
    }

    public long getCamp1FamilyId() {
        return camp1FamilyId;
    }

    public void setCamp1FamilyId(long camp1FamilyId) {
        this.camp1FamilyId = camp1FamilyId;
    }

    public long getCamp2FamilyId() {
        return camp2FamilyId;
    }

    public void setCamp2FamilyId(long camp2FamilyId) {
        this.camp2FamilyId = camp2FamilyId;
    }

    public String getCamp1FamilyName() {
        return camp1FamilyName;
    }

    public void setCamp1FamilyName(String camp1FamilyName) {
        this.camp1FamilyName = camp1FamilyName;
    }

    public String getCamp2FamilyName() {
        return camp2FamilyName;
    }

    public void setCamp2FamilyName(String camp2FamilyName) {
        this.camp2FamilyName = camp2FamilyName;
    }

    public int getCamp2MainServerId() {
        return camp2MainServerId;
    }

    public void setCamp2MainServerId(int camp2MainServerId) {
        this.camp2MainServerId = camp2MainServerId;
    }

    public int getCamp1MainServerId() {
        return camp1MainServerId;
    }

    public void setCamp1MainServerId(int camp1MainServerId) {
        this.camp1MainServerId = camp1MainServerId;
    }

    public Map<String, FighterEntity> getCamp1FighterMap() {
        return camp1FighterMap;
    }

    public void setCamp1FighterMap(Map<String, FighterEntity> camp1FighterMap) {
        this.camp1FighterMap = camp1FighterMap;
        this.fighterMap.putAll(camp1FighterMap);
    }

    public Map<String, FighterEntity> getCamp2FighterMap() {
        return camp2FighterMap;
    }

    public void setCamp2FighterMap(Map<String, FighterEntity> camp2FighterMap) {
        this.camp2FighterMap = camp2FighterMap;
        this.fighterMap.putAll(camp2FighterMap);
    }

    public Map<String, Map<String, Long>> getSufferedDamageMap() {
        return sufferedDamageMap;
    }

    public void setSufferedDamageMap(Map<String, Map<String, Long>> sufferedDamageMap) {
        this.sufferedDamageMap = sufferedDamageMap;
    }

    public Map<String, EliteFightTower> getTowerMap() {
        return towerMap;
    }

    public void setTowerMap(Map<String, EliteFightTower> towerMap) {
        this.towerMap = towerMap;
    }

    public long getCamp1TotalFightScore() {
        return camp1TotalFightScore;
    }

    public void setCamp1TotalFightScore(long camp1TotalFightScore) {
        this.camp1TotalFightScore = camp1TotalFightScore;
    }

    public long getCamp2TotalFightScore() {
        return camp2TotalFightScore;
    }

    public void setCamp2TotalFightScore(long camp2TotalFightScore) {
        this.camp2TotalFightScore = camp2TotalFightScore;
    }

    public void addPayReviveCount(String fighterUid) {
        int oldCount = 0;
        if (payReviveCountMap.containsKey(fighterUid)) {
            oldCount = payReviveCountMap.get(fighterUid);
        }
        payReviveCountMap.put(fighterUid, oldCount + 1);
    }

    public FamilyWarEliteFightStat getStat() {
        return stat;
    }

    public boolean playOrAI(long roleId) {
        String fighterId = Long.toString(roleId);
        return reviveStateMap.get(fighterId) == FamilyWarConst.play;
    }

    public List<Long> getFightIds(){
        List<Long> fightIds = new ArrayList<>();
        for (Entry<String, Integer> entry : reviveStateMap.entrySet()) {
            if (entry.getValue() == FamilyWarConst.play){
                fightIds.add(Long.valueOf(entry.getKey()));
            }
        }
        return fightIds;
    }
}
