package com.stars.multiserver.familywar.knockout.fight.normal;

import com.stars.core.attr.Attribute;
import com.stars.coreManager.ExcutorKey;
import com.stars.coreManager.SchedulerManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.drop.DropManager;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.modules.familyactivities.war.event.FamilyWarFighterAddingSucceededEvent;
import com.stars.modules.familyactivities.war.packet.ClientFamilyWarBattleFightKillCount;
import com.stars.modules.familyactivities.war.packet.ClientFamilyWarBattleFightPersonalPoint;
import com.stars.modules.familyactivities.war.packet.ClientFamilyWarBattleFightRevive;
import com.stars.modules.familyactivities.war.packet.ClientFamilyWarBattleStat;
import com.stars.modules.familyactivities.war.packet.fight.ClientFamilyWarFightNormalOpponentInfo;
import com.stars.modules.pk.event.BackCityEvent;
import com.stars.modules.pk.packet.ClientUpdatePlayer;
import com.stars.modules.pk.packet.ServerOrder;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterPK;
import com.stars.modules.scene.prodata.DynamicBlock;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.FamilyWarRpcHelper;
import com.stars.multiserver.familywar.FamilyWarUtil;
import com.stars.multiserver.familywar.event.FamilyWarSendPacketEvent;
import com.stars.multiserver.familywar.knockout.FamilyWarKnockoutBattle;
import com.stars.multiserver.familywar.knockout.fight.elite.EliteFightTower;
import com.stars.multiserver.fight.ServerOrders;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.network.PacketUtil;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;
import com.stars.services.fightServerManager.FSManagerServiceActor;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;
import io.netty.buffer.Unpooled;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.stars.modules.familyactivities.war.FamilyActWarManager.*;
import static com.stars.multiserver.familywar.FamilyWarConst.K_CAMP1;
import static com.stars.multiserver.familywar.FamilyWarConst.K_CAMP2;
import static java.lang.Long.parseLong;

/**
 * （玩家，机器人）杀人（玩家，机器人） -> 士气, 积分
 * 胜利 -> 士气
 * Created by zhaowenshuo on 2016/11/15.
 */
public class FamilyWarNormalFight {

    private FamilyWarKnockoutBattle battle;

    /* 战斗服相关 */
    private String fightId;
    private int fightServerId;
    private long creationTimestamp;

    private long camp1FamilyId;
    private long camp2FamilyId;
    private String camp1FamilyName;
    private String camp2FamilyName;
    private int camp1MainServerId;
    private int camp2MainServerId;

    private Map<String, FighterEntity> camp1FighterMap;
    private Map<String, FighterEntity> camp2FighterMap;
    private Map<String, FighterEntity> fighterMap;
    private Map<Long, Byte> campMap;
    //    private Set<String> camp1SurvivalSet;
//    private Set<String> camp2SurvivalSet;
    private Set<String> teamSheet;//精英成员过来打匹配
    private int invincibleBuffInstId;

    private Map<String, Map<String, Long>> sufferedDamageMap = new HashMap<>(); // (受害者, (攻击者, 伤害值))
    private Map<String, Integer> comboKillCountMap = new HashMap<>(); // (roleId, 连杀数)
    private FamilyWarNormalFightStat stat;
    private Map<String, EliteFightTower> towerMap = new HashMap<>();
    private Map<Long, List<Long>> fighterIdListMap; // (faimlyId, list of fighter id)
    /* 复活相关（真TM蛋疼） */
    private ConcurrentHashMap<String, Long> reviveMap;
    private Map<String, Integer> payReviveCountMap;
    private Map<String, Integer> reviveStateMap; // uid -- state(1:真人,0:机器人)
    private int battleType;
    private List<FighterEntity> playerList;

    public FamilyWarNormalFight(
            String fightId, long camp1FamilyId, String camp1FamilyName, long camp2FamilyId, String camp2FamilyName, int camp1MainServerId, int camp2MainServerId,
            Map<String, FighterEntity> camp1FighterMap, Map<String, FighterEntity> camp2FighterMap,
            FamilyWarKnockoutBattle battle, Set<String> teamSheet) {
        this.fightId = fightId;
        this.fightServerId = ServiceHelper.fsManagerService().getFightServer(FSManagerServiceActor.FIGHT_SERVER_LEVEL_COMM);
        this.creationTimestamp = System.currentTimeMillis();
        this.camp1FamilyId = camp1FamilyId;
        this.camp1FamilyName = camp1FamilyName;
        this.camp2FamilyId = camp2FamilyId;
        this.camp2FamilyName = camp2FamilyName;
        this.camp1MainServerId = camp1MainServerId;
        this.camp2MainServerId = camp2MainServerId;
        this.camp1FighterMap = camp1FighterMap;
        this.camp2FighterMap = camp2FighterMap;
        this.fighterMap = new HashMap<>();
        this.fighterMap.putAll(camp1FighterMap);
        this.fighterMap.putAll(camp2FighterMap);
        this.campMap = new HashMap<>();
        this.battle = battle;
//        this.camp1SurvivalSet = new HashSet<>(camp1FighterMap.keySet());
//        this.camp2SurvivalSet = new HashSet<>(camp2FighterMap.keySet());
        this.sufferedDamageMap = new HashMap<>();
        this.reviveMap = new ConcurrentHashMap<>();
        this.payReviveCountMap = new HashMap<>();
        this.reviveStateMap = new HashMap<>();
        this.teamSheet = teamSheet;
        fighterIdListMap = new HashMap<>();
        fighterIdListMap.put(camp1FamilyId, new ArrayList<Long>());
        fighterIdListMap.put(camp2FamilyId, new ArrayList<Long>());
        // 设置camp
        for (FighterEntity entity : fighterMap.values()) {
            entity.setState((byte) 0);
            if (camp1FighterMap.containsKey(entity.getUniqueId())) {
                entity.setCamp(FamilyWarConst.K_CAMP1);
                this.campMap.put(Long.parseLong(entity.getUniqueId()), FamilyWarConst.K_CAMP1);
                fighterIdListMap.get(camp1FamilyId).add(Long.parseLong(entity.getUniqueId()));
            }
            if (camp2FighterMap.containsKey(entity.getUniqueId())) {
                entity.setCamp(FamilyWarConst.K_CAMP2);
                this.campMap.put(Long.parseLong(entity.getUniqueId()), FamilyWarConst.K_CAMP2);
                fighterIdListMap.get(camp2FamilyId).add(Long.parseLong(entity.getUniqueId()));
            }
            if (entity.getExtraValue().equals("isAuto=1")) {
                reviveStateMap.put(entity.getUniqueId(), FamilyWarConst.AI);
            } else {
                reviveStateMap.put(entity.getUniqueId(), FamilyWarConst.play);
//                entity.setExtraValue("isAuto=1");
            }
        }
        // 设置统计值
        this.stat = new FamilyWarNormalFightStat();
        this.stat.setCamp1FamilyId(camp1FamilyId);
        this.stat.setCamp2FamilyId(camp2FamilyId);
        this.stat.setCamp1FamilyName(camp1FamilyName);
        this.stat.setCamp2FamilyName(camp2FamilyName);
        this.stat.setCamp1ServerId(camp1MainServerId);
        this.stat.setCamp2ServerId(camp2MainServerId);
        this.stat.setCamp1FightScore(battle.getCampFightScore(camp1FamilyId));
        this.stat.setCamp2FightScore(battle.getCampFightScore(camp2FamilyId));
        for (FighterEntity entity : fighterMap.values()) {
            long fighterId = parseLong(entity.getUniqueId());
            stat.addPersonalStat(fighterId, entity.getName(), entity.getCamp(), entity.getAttribute().getHp());
        }
    }

    public void start(List<FamilyWarNormalFightStat> statList, int battleType) {
        this.battleType = battleType;
        FamilyWarNormalFightArgs args = new FamilyWarNormalFightArgs();
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
        for (FighterEntity entity : fighterMap.values()) {
            maxFightSocre = entity.getFightScore() > maxFightSocre ? entity.getFightScore() : maxFightSocre;
        }

        Map<String, FighterEntity> nonPlayerEntity = FamilyWarUtil.getMonsterFighterEntity(stageIdOfEliteFight);
        for (FighterEntity entity : nonPlayerEntity.values()) {
            Byte towerType = towerTypeMap.get(entity.getSpawnConfigId());
            if (towerType == null) {
                continue;
            }
            printTowerAttr(entity, "计算前|normal|成员最高战力" + maxFightSocre);
            entity.getAttribute().setAttack((int) (entity.getAttribute().getAttack() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_attack_zzz));
            entity.getAttribute().setHp((int) (entity.getAttribute().getHp() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_hp_zzz));
            entity.getAttribute().setMaxhp((int) (entity.getAttribute().getMaxhp() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_hp_zzz));
            entity.getAttribute().setDefense((int) (entity.getAttribute().getDefense() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_defense_zzz));
            entity.getAttribute().setHit((int) (entity.getAttribute().getHit() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_hit_zzz));
            entity.getAttribute().setAvoid((int) (entity.getAttribute().getAvoid() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_avoid_zzz));
            entity.getAttribute().setCrit((int) (entity.getAttribute().getCrit() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_crit_zzz));
            entity.getAttribute().setAnticrit((int) (entity.getAttribute().getAnticrit() / 10000.0 * maxFightSocre * FamilyActWarManager.familywar_coefficient_anticrit_zzz));
            printTowerAttr(entity, "计算后|normal|");
            EliteFightTower tower = new EliteFightTower(entity.getUniqueId(), entity.getCamp(), towerType, entity.getPosition(), entity.getAttribute().getMaxhp());
            towerMap.put(tower.getUid(), tower);
        }

        StageinfoVo stageVo = SceneManager.getStageVo(stageIdOfNormalFight);
        ClientEnterPK enterPacket = new ClientEnterPK();
        enterPacket.setFightType(SceneManager.SCENETYPE_FAMILY_WAR_NORMAL_FIGHT);
        enterPacket.setStageId(stageIdOfNormalFight);
        enterPacket.setLimitTime(timeLimitOfNormalFight);
        enterPacket.setCountdownOfBegin(timeOfNoramlFightWaiting);
        enterPacket.setSkillVoMap(FamilyWarUtil.getAllRoleSkillVoMap());
        enterPacket.addMonsterVoMap(stageVo.getMonsterVoMap());
        battle.fightService().createFight(fightServerId, FightConst.T_FAMILY_WAR_NORMAL_FIGHT,
                MultiServerHelper.getServerId(), fightId, PacketUtil.packetToBytes(enterPacket), args);

        battle.fightService().addMonster(fightServerId, FightConst.T_FAMILY_WAR_NORMAL_FIGHT,
                MultiServerHelper.getServerId(), fightId, new ArrayList<>(nonPlayerEntity.values()));
        statList.add(stat);
        addInvincibleBuff();
    }

    private void getBlockMap(ClientEnterPK enterPacket, StageinfoVo stageVo) {
        Map<String, Byte> blockStatus = new HashMap<>();

        for (DynamicBlock dynamicBlock : stageVo.getDynamicBlockMap().values()) {
            blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_CLOSE);
            if (dynamicBlock.getShowSpawnId() == 0) {
                blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_OPEN);
            }
        }
        enterPacket.setBlockMap(stageVo.getDynamicBlockMap());
        enterPacket.addBlockStatusMap(blockStatus);
    }

    private void printTowerAttr(FighterEntity entity, String text) {
        Attribute attribute = entity.getAttribute();
        LogUtil.info("familywar|normal|塔属性:{}|uid:{},hp:{},attack:{},anticrit:{},avoid:{},crit:{},defense:{},hit:{}"
                , text, entity.getUniqueId(), attribute.getHp(), attribute.getAttack(), attribute.getAnticrit(), attribute.getAvoid(), attribute.getCrit(), attribute.getDefense(), attribute.getHit());
    }

    public void enterNormalFight(int mainServerId, long familyId, long roleId, FighterEntity entity) {
        String fighterId = Long.toString(roleId);
        FighterEntity myEntity = fighterMap.put(fighterId, entity);
        myEntity.setState((byte) 0);//每次进入都设置没复活过
        myEntity.setExtraValue("");
        sendStatPacket(roleId);
        List<FighterEntity> list = new ArrayList<>();
        list.add(myEntity);
        battle.fightService().addFighter(fightServerId,
                FightConst.T_FAMILY_WAR_NORMAL_FIGHT, MultiServerHelper.getServerId(), fightId, list);
        changePlayerState(roleId, FamilyWarConst.play);
        ArrayList<String> entityKey = new ArrayList<>();
        entityKey.add(entity.getUniqueId());
        ServerOrder order = ServerOrders.newAiOrder(ServerOrder.CLOSE_AI, entityKey);
        battle.fightService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                mainServerId, fightId, order);
    }

    public void end() {
        accCampData();
        if (stat.getCamp1TowerHp() != stat.getCamp2TowerHp()) {
            long winnerFamilyId = stat.getCamp1TowerHp() > stat.getCamp2TowerHp() ? camp1FamilyId : camp2FamilyId;
            long loserFamilyId = stat.getCamp1TowerHp() > stat.getCamp2TowerHp() ? camp2FamilyId : camp1FamilyId;
            finish(winnerFamilyId, loserFamilyId, FamilyWarConst.WIN_BY_TOWER_HP);
            return;
        }
        if (stat.getCamp1FightScore() != stat.getCamp2FightScore()) {
            long winnerFamilyId = stat.getCamp1FightScore() > stat.getCamp2FightScore() ? camp1FamilyId : camp2FamilyId;
            long loserFamilyId = stat.getCamp1FightScore() > stat.getCamp2FightScore() ? camp2FamilyId : camp1FamilyId;
            finish(winnerFamilyId, loserFamilyId, FamilyWarConst.WIN_BY_CAMP_FIGHTSCORE);
            return;
        }
        finish(camp1FamilyId, camp2FamilyId, FamilyWarConst.WIN_BY_CAMP_FIGHTSCORE);
    }

    private void accCampData() {
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
        long camp1TotalFightScore = battle.getCampFightScore(camp1FamilyId);
        long camp2TotalFightScore = battle.getCampFightScore(camp2FamilyId);
        LogUtil.info("familywar|normalfight结算时塔的血量 camp1Tower :{} ,camp1TowerHp:{} ,camp1TowerTotalHp:{}, camp2Tower:{}, camp2TowerHp:{}, camp2TowerTotalHp:{}",
                camp1Tower, camp1TowerHp, camp1TowerTotalHp, camp2Tower, camp2TowerHp, camp2TowerTotalHp);
        stat.setCamp1TowerHp(camp1Tower);
        stat.setCamp2TowerHp(camp2Tower);
        stat.setCamp1FightScore(camp1TotalFightScore);
        stat.setCamp2FightScore(camp2TotalFightScore);
    }

    public void stopFight(boolean backCity) {
        battle.restoreFighterState(camp1FamilyId, camp1FighterMap.keySet());
        battle.restoreFighterState(camp2FamilyId, camp2FighterMap.keySet());
        battle.fightService().stopFight(
                fightServerId,
                FightConst.T_FAMILY_WAR_NORMAL_FIGHT,
                MultiServerHelper.getServerId(),
                fightId);
        if (backCity) { //强制回城
            for (String roleId : camp1FighterMap.keySet()) {
                FighterEntity entity = camp1FighterMap.get(roleId);
                if (entity != null && entity.getFighterType() != FighterEntity.TYPE_MONSTER
                        && entity.getFighterType() != FighterEntity.TYPE_ROBOT) {
                    // 抛事件
                    battle.roleService().notice(camp1MainServerId, Long.parseLong(roleId), new BackCityEvent());
                }
            }
            for (String roleId : camp2FighterMap.keySet()) {
                FighterEntity entity = camp2FighterMap.get(roleId);
                if (entity != null && entity.getFighterType() != FighterEntity.TYPE_MONSTER
                        && entity.getFighterType() != FighterEntity.TYPE_ROBOT) {
                    // 抛事件
                    battle.roleService().notice(camp2MainServerId, Long.parseLong(roleId), new BackCityEvent());
                }
            }
        }
    }

    public void onFightCreationSucceeded() {
        this.creationTimestamp = System.currentTimeMillis();
        LogUtil.info("familywar|3v3 匹配战场创建成功 camp1:{},camp2:{}", camp1FamilyId, camp2FamilyId);
        ClientFamilyWarFightNormalOpponentInfo packet = new ClientFamilyWarFightNormalOpponentInfo();
        packet.setCamp1FamilyName(camp1FamilyName);
        packet.setCamp2FamilyName(camp2FamilyName);
        packet.setCamp1ServerName(MultiServerHelper.getServerName(camp1MainServerId));
        packet.setCamp2ServerName(MultiServerHelper.getServerName(camp2MainServerId));
        List<FighterEntity> playerList = new ArrayList<>();
        for (FighterEntity entity : fighterMap.values()) {
            playerList.add(entity);
            packet.addOpponentInfo(Long.parseLong(entity.getUniqueId()), entity.getName(), entity.getFightScore(), entity.getModelId(), entity.getCamp());
        }
        this.playerList = playerList;
        for (String roleId : camp1FighterMap.keySet()) {
            FighterEntity entity = camp1FighterMap.get(roleId);
            if (entity != null && entity.getFighterType() != FighterEntity.TYPE_MONSTER
                    && entity.getFighterType() != FighterEntity.TYPE_ROBOT) {
                battle.roleService().send(camp1MainServerId, Long.parseLong(roleId), packet);
                sendStatPacket(Long.parseLong(roleId));
                battle.roleService().notice(camp1MainServerId, Long.parseLong(roleId), new FamilyWarFighterAddingSucceededEvent(fightServerId, SceneManager.SCENETYPE_FAMILY_WAR_NORMAL_FIGHT));
            }
        }
        for (String roleId : camp2FighterMap.keySet()) {
            FighterEntity entity = camp2FighterMap.get(roleId);
            if (entity != null && entity.getFighterType() != FighterEntity.TYPE_MONSTER
                    && entity.getFighterType() != FighterEntity.TYPE_ROBOT) {
                battle.roleService().send(camp2MainServerId, Long.parseLong(roleId), packet);
                sendStatPacket(Long.parseLong(roleId));
                battle.roleService().notice(camp2MainServerId, Long.parseLong(roleId), new FamilyWarFighterAddingSucceededEvent(fightServerId, SceneManager.SCENETYPE_FAMILY_WAR_NORMAL_FIGHT));
            }
        }
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.SCHEDULE_KEY_NORMAL + "-" + fightId, new Runnable() {
            @Override
            public void run() {
                addFighter();
            }
        }, 2, 100, TimeUnit.SECONDS);

    }

    private void addFighter() {
        LogUtil.info("normal|addFighter");
        SchedulerManager.shutDownNow(ExcutorKey.SCHEDULE_KEY_NORMAL + "-" + fightId);
        battle.fightService().addFighter(fightServerId,
                FightConst.T_FAMILY_WAR_NORMAL_FIGHT, MultiServerHelper.getServerId(), fightId, playerList);
    }

    private void sendStatPacket(long roleId) {
        ClientFamilyWarBattleStat packet = new ClientFamilyWarBattleStat();
        FamilyWarNormalFightPersonalStat personalStat = stat.getPersonalStatMap().get(roleId);
        packet.setMyKillCount(personalStat.getKillCount());
        packet.setMyDeadCount(personalStat.getDeadCount());
        packet.setMyAssistCount(personalStat.getAssistCount());
        battle.roleService().send(getMainServerId(Long.toString(roleId)), roleId, packet);
        syncPersonalPoints(Long.toString(roleId));
    }

    // 胜利奖励 - 胜利的一方，且没有死
    // 失败奖励 - 失败的一方，或死掉
    // 没有奖励 - 掉线
    public void finish(long winnerFamilyId, long loserFamilyId, int finishType) {
        stat.setWinnerFamilyId(winnerFamilyId);
        stat.setLoserFamilyId(loserFamilyId);
        // stop fight actor
        stopFight(false);
        updatePersonalPoints(winnerFamilyId == camp1FamilyId ? camp1FighterMap : camp2FighterMap);
        accumulateAwardAndSend(winnerFamilyId, getCampFighter(winnerFamilyId), dropIdOfNormalFightWinAward, familywar_smallwinscore, true);
        accumulateAwardAndSend(loserFamilyId, getCampFighter(loserFamilyId), dropIdOfNormalFightLoseAward, familywar_smallfailscore, false);
        battle.finishNormalFight(fightId, winnerFamilyId, loserFamilyId, stat, finishType);
    }

    private Set<String> getCampFighter(long familyId) {
        if (familyId == camp1FamilyId) {
            return camp1FighterMap.keySet();
        } else if (familyId == camp2FamilyId) {
            return camp2FighterMap.keySet();
        } else {
            return new HashSet<>();
        }
    }

    private void updatePersonalPoints(Map<String, FighterEntity> fighterMap) {
        for (String fighterId : fighterMap.keySet()) {
            updatePersonalPoints(fighterId, familywar_score_pairwin);
        }
    }

    private void updatePersonalPoints(String fighterId, long pointDelta) {
        if (!teamSheet.contains(fighterId)) {
            battle.updateNormalPoints(fighterId, pointDelta);
        } else {
            battle.updateElitePoints(fighterId, pointDelta);
        }
        stat.updatePersonalStat(Long.parseLong(fighterId), pointDelta);
    }

    private void accumulateAwardAndSend(long familyId, Set<String> survivalSet, int dropId, int moraleDelta, boolean win) {
        for (String fighterId : survivalSet) {
            // 从战场中玩家移除
            battle.removeFighterFromBattle(familyId, Long.parseLong(fighterId));
            FighterEntity entity = fighterMap.get(fighterId);
            if (FamilyWarUtil.isPlayer(entity)) {
                // 计算受害者的奖励
                Map<Integer, Integer> toolMap = DropManager.executeDrop(dropId, 1);
                battle.accumulateNormalFightAward(Long.parseLong(fighterId), toolMap);
                stat.addPersonalToolMap(Long.parseLong(fighterId), toolMap);
                // 同步界面
                FamilyWarNormalFightPersonalStat personalStat = stat.getPersonalStatMap().get(Long.parseLong(fighterId));
                battle.logEvent(FamilyWarConst.normalWarLog, win ? FamilyWarConst.successLog : FamilyWarConst.failLog,
                        battle.getFamilyWar() == null ? 0 : battle.getFamilyWar().getNormalPointsRankList(Long.parseLong(fighterId)).getRank(fighterId), personalStat.getPoints(), 0,
                        personalStat.getFighterId(), battle.getBattleType() == FamilyWarConst.W_TYPE_LOCAL ? 1 : 2, battle.getBattleType() == FamilyWarConst.W_TYPE_QUALIFYING ? 1 : 2, toolMap);
            }
        }
    }

    public void handleDamage(Map<String, HashMap<String, Integer>> damageMap) {
        for (Map.Entry<String, HashMap<String, Integer>> entry : damageMap.entrySet()) {
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
                    for (int damage : victimSufferedDamageMap.values()) {
                        tower.reduceHp(damage);
                    }
                }
            }
        }
    }

    private Map<String, Long> toStringLongMap(Map<String, Integer> map) {
        Map<String, Long> newMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue().longValue());
        }
        return newMap;
    }

    public void handleDead(Map<String, String> deadMap) {
        for (Map.Entry<String, String> entry : deadMap.entrySet()) {
            String victimId = entry.getKey();
            String attackerUid = entry.getValue();
            if (towerMap.containsKey(victimId)) {
                EliteFightTower tower = towerMap.get(victimId);
                if (tower.getType() == FamilyWarConst.K_TOWER_TYPE_CRYSTAL) {
                    handleCrystalDead(tower, attackerUid);
                } else {
                    handleTowerDead(tower, attackerUid);
                }
            } else {
                handleFighterDead(entry.getKey(), entry.getValue());
            }
        }
    }

    private void handleTowerDead(EliteFightTower tower, String attackerUid) {
// 检查状态
        // 强制将血量改成0
        tower.setHp(0);
        // 加积分，加士气
        battle.updateMorale(getFamilyId(attackerUid), moraleDeltaOfDestoryTower); // 己方增加士气
        battle.updateMorale(getOpponentFamilyId(getFamilyId(attackerUid)), -moraleDeltaOfLosingTower); // 对方减少士气
        updatePersonalPoints(attackerUid, pointsDeltaOfDestoryTower);
        syncPersonalPoints(attackerUid);
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
        battle.fightService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_NORMAL_FIGHT,
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
        battle.fightService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_NORMAL_FIGHT,
                MultiServerHelper.getServerId(), fightId, order);
    }

    public void changePlayerState(long roleId, int state) {
        reviveStateMap.put(Long.toString(roleId), state);
        if (state == FamilyWarConst.AI) {
            FighterEntity entity = fighterMap.get(Long.toString(roleId));
            entity.setExtraValue("isAuto=1");
        }
    }

    public void handlePlayerRevive(long roleId, int state) {
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
            for (Map.Entry<String, Long> entry : tmpReviveMap.entrySet()) {
                if (now - entry.getValue() > (homeReviveTime + 2) * 1000) {
                    revive(entry.getKey(), homeRevive);
                }
            }
        } catch (Exception e) {
            LogUtil.error("familywar|家族战复活异常:" + e.getMessage());
        }
    }

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
        LogUtil.info(" {} 复活啦", fighterUid);
        battle.fightService().addFighter(fightServerId, FightConst.T_FAMILY_WAR_NORMAL_FIGHT,
                MultiServerHelper.getServerId(), fightId, entityList);
        //发送满血满状态指令
        ServerOrder serverOrder = new ServerOrder();
        serverOrder.setOrderType(ServerOrder.ORDER_TYPE_RESET_CHARACS);
        serverOrder.setUniqueIDs(reviveList);
        sendServerOrder(fightId, serverOrder);//发送服务端lua命令
    }

    public byte getFighterReviveState(String fighterUid) {
        FighterEntity entity = fighterMap.get(fighterUid);
        if (entity == null) return 0;
        return entity.getState();
    }

    /**
     * 发送服务端lua命令
     */
    private void sendServerOrder(String fightId, ServerOrder serverOrder) {
        ClientUpdatePlayer packet = new ClientUpdatePlayer();
        packet.addOrder(serverOrder);
        byte[] bytes = packetToBytes(packet);
        if (battleType == FamilyWarConst.W_TYPE_LOCAL) {
            MainRpcHelper.fightBaseService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_NORMAL_FIGHT,
                    MultiServerHelper.getServerId(), fightId, bytes);
        } else if (battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
            FamilyWarRpcHelper.fightBaseService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_NORMAL_FIGHT,
                    MultiServerHelper.getServerId(), fightId, bytes);
        } else if (battleType == FamilyWarConst.W_TYPE_REMOTE) {
            FamilyWarRpcHelper.fightBaseService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_NORMAL_FIGHT,
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

    private void handleCrystalDead(EliteFightTower tower, String attackerUid) {
        long winnerFamilyId = getFamilyId(attackerUid);
        long loserFamilyId = winnerFamilyId == camp1FamilyId ? camp2FamilyId : camp1FamilyId;
        accCampData();
        finish(winnerFamilyId, loserFamilyId, FamilyWarConst.WIN_BY_CRYSTAL_DEAD);
//        battle.removeBattle();
//        battle.end(fightId, winnerFamilyId, loserFamilyId, stat, true);
    }

    public void handleFighterDead(String victimUid, String attackerUid) {
        // 从幸存者集合中移除
        long victimFamilyId = getFamilyId(victimUid);
//        Set<String> victimSurvival = victimFamilyId == camp1FamilyId ? camp1SurvivalSet : camp2SurvivalSet;
//        victimSurvival.remove(victimUid);
//        battle.removeFighterFromBattle(victimFamilyId, Long.parseLong(victimUid));
        // 增加士气


        if (fighterMap.containsKey(attackerUid) && fighterMap.containsKey(victimUid)) {
            FighterEntity victimEntity = fighterMap.get(victimUid);
            FighterEntity attackerEntity = fighterMap.get(attackerUid);
            if (reviveStateMap.get(attackerUid) == FamilyWarConst.play) {
                battle.roleService().warn(getMainServerId(attackerUid), parseLong(attackerUid),
                        attackerEntity.getName() + "杀死" + victimEntity.getName());
            }
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
//                long victimFamilyId = getFamilyId(victimUid);
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
                for (long roleId : fighterIdListMap.get(victimFamilyId)) {
                    if (reviveStateMap.get(Long.toString(roleId)) == FamilyWarConst.play) {
                        battle.roleService().notice(getMainServerId(victimUid), roleId, new FamilyWarSendPacketEvent(packet, FamilyWarConst.SCENE_TYPE_FAMILY_WAR));
                    }
                }
            }
            comboKillCountMap.put(victimUid, 0);
            battle.updateMorale(getFamilyId(attackerUid), moraleDeltaOfKillFighterInNormalFight);
            // 计算积分
            Map<String, Long> damageMap = sufferedDamageMap.get(victimUid);
            if (damageMap != null) {
                long totalDamage = MapUtil.sum(damageMap, 0L);
                for (Map.Entry<String, Long> entry : damageMap.entrySet()) {
                    FighterEntity entity = fighterMap.get(entry.getKey());
                    if (entity == null || FamilyWarUtil.isRobot(entity)) continue;
                    double ratio = (entry.getValue() * 1.0) / totalDamage;
                    LogUtil.info("{} 最后一击|{} 对 {} 造成 {} 伤害,总伤害:{}", attackerUid, entry.getKey(), victimFamilyId, entry.getValue(), totalDamage);
                    if (ratio >= damageRatioThresholdOfPersonalPoints) {
                        long pointsDelta = (long) (ratio * coefficientD + coefficientDD);
                        updatePersonalPoints(entry.getKey(), pointsDelta);
                        syncPersonalPoints(entry.getKey());
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

        reviveMap.put(victimUid, System.currentTimeMillis());

        // 发送复活框
        Integer payReviveCount = payReviveCountMap.get(victimUid);
        ClientFamilyWarBattleFightRevive packet = new ClientFamilyWarBattleFightRevive();
        packet.setType(ClientFamilyWarBattleFightRevive.TYPE_OF_COUNT);
        packet.setReviveCount(payReviveCount == null ? 0 : payReviveCount.intValue());
        if (reviveStateMap.get(victimUid) == FamilyWarConst.play) {
            battle.roleService().send(getMainServerId(victimUid), Long.parseLong(victimUid), packet);
        }

//        FighterEntity victimEntity = fighterMap.get(victimUid);
//        if (FamilyWarUtil.isPlayer(victimEntity)) { // 不是机器人的情况下，才发结算包
//            // 计算受害者的奖励
//            Map<Integer, Integer> toolMap = DropManager.executeDrop(dropIdOfNormalFightLoseAward, 1);
//            battle.accumulateNormalFightAward(Long.parseLong(victimUid), toolMap);
//            // 同步界面
//            FamilyWarNormalFightPersonalStat personalStat = stat.getPersonalStatMap().get(Long.parseLong(victimUid));
//            ClientFamilyWarFightNormalResult packet = new ClientFamilyWarFightNormalResult();
//            packet.setIsElite((byte) (teamSheet.contains(Long.toString(personalStat.getFighterId())) ? 1 : 0));
//            packet.setWin(false);
//            packet.setMoraleDelta(0);
//            packet.setPoints(personalStat.getPoints());
//            packet.setToolMap(toolMap);
//            battle.roleService().send(getMainServerId(victimUid), Long.parseLong(victimUid), packet);
//            battle.logEvent(FamilyWarConst.normalWarLog, FamilyWarConst.failLog, battle.getFamilyWar() == null ? 0 : battle
//                            .getFamilyWar().getNormalPointsRankList(Long.parseLong(victimUid)).getRank(victimUid), personalStat.getPoints(), 0,
//                    personalStat.getFighterId(), battle.getBattleType() == FamilyWarConst.W_TYPE_LOCAL ? 1 : 2, battle.getBattleType() == FamilyWarConst.W_TYPE_QUALIFYING ? 1 : 2, toolMap);
//        }
    }

    public void syncPersonalPoints(String fighterUid) {
        long roleId = Long.parseLong(fighterUid);

        long point = 0;
        if (!teamSheet.contains(fighterUid)) {
            point = battle.getBattleNormalPersonalPosints(fighterUid);
        } else {
            point = battle.getElitePoint(fighterUid);
        }
        battle.roleService().send(getMainServerId(fighterUid), roleId,
                new ClientFamilyWarBattleFightPersonalPoint(point));
    }

    public boolean checkTimeout() {
        return System.currentTimeMillis() - creationTimestamp >= timeLimitOfNormalFight * 1000;
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

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public boolean inFight(long roleId) {
        return fighterMap.containsKey(Long.toString(roleId));
    }

    public String getFightId() {
        return fightId;
    }

    public Map<String, EliteFightTower> getTowerMap() {
        return towerMap;
    }

    public List<Long> getFightIds() {
        List<Long> fightIds = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : reviveStateMap.entrySet()) {
            if (entry.getValue() == FamilyWarConst.play) {
                fightIds.add(Long.parseLong(entry.getKey()));
            }
        }
        return fightIds;
    }
}
