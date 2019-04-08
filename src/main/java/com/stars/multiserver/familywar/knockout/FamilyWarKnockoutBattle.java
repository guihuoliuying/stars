package com.stars.multiserver.familywar.knockout;

import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropManager;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.modules.familyactivities.war.event.FamilyWarLogEvent;
import com.stars.modules.familyactivities.war.packet.ClientFamilyWarBattleFamilyPoints;
import com.stars.modules.familyactivities.war.packet.ClientFamilyWarBattleFightInitInfo;
import com.stars.modules.familyactivities.war.packet.ClientFamilyWarBattleFightUpdateInfo;
import com.stars.modules.familyactivities.war.packet.fight.ClientFamilyWarBattleFightEliteResult;
import com.stars.modules.familyactivities.war.packet.fight.ClientFamilyWarBattleResult;
import com.stars.modules.familyactivities.war.packet.fight.ClientFamilyWarFightNormalResult;
import com.stars.modules.familyactivities.war.packet.ui.ClientFamilyWarUiEnter;
import com.stars.modules.familyactivities.war.prodata.FamilyWarMoraleVo;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.FamilyWar;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.FamilyWarOnlinePlayerMap;
import com.stars.multiserver.familywar.FamilyWarUtil;
import com.stars.multiserver.familywar.event.FamilyWarSendPacketEvent;
import com.stars.multiserver.familywar.knockout.fight.elite.EliteFightTower;
import com.stars.multiserver.familywar.knockout.fight.elite.FamilyWarEliteFight;
import com.stars.multiserver.familywar.knockout.fight.elite.FamilyWarEliteFightPersonalStat;
import com.stars.multiserver.familywar.knockout.fight.elite.FamilyWarEliteFightStat;
import com.stars.multiserver.familywar.knockout.fight.normal.FamilyWarNormalFight;
import com.stars.multiserver.familywar.knockout.fight.normal.FamilyWarNormalFightPersonalStat;
import com.stars.multiserver.familywar.knockout.fight.normal.FamilyWarNormalFightStat;
import com.stars.multiserver.familywar.knockout.fight.stage.FamilyWarStageFight;
import com.stars.multiserver.familywar.qualifying.FamilyWarQualifying;
import com.stars.multiserver.familywar.remote.FamilyWarRemote;
import com.stars.multiserver.fight.FightIdCreator;
import com.stars.multiserver.fightutil.FightStat;
import com.stars.services.ServiceHelper;
import com.stars.services.activities.ActConst;
import com.stars.services.fightServerManager.FSManagerServiceActor;
import com.stars.services.fightbase.FightBaseService;
import com.stars.services.role.RoleService;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryComponent;
import com.stars.util.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import static com.stars.modules.familyactivities.war.FamilyActWarManager.*;
import static com.stars.multiserver.familywar.FamilyWarConst.K_CAMP1;
import static com.stars.multiserver.familywar.FamilyWarConst.K_CAMP2;

/**
 * Created by zhaowenshuo on 2016/11/16.
 */
public class FamilyWarKnockoutBattle {

    /*  */
    private FamilyWar familyWar;

    private String battleId;
    private int type;

    private int battleType;
    /* 家族/人员管理 */
    private long camp1FamilyId;
    private long camp2FamilyId;
    private int camp1MainServerId;
    private int camp2MainServerId;
    private Map<Long, FamilyBattleInfo> familyInfoMap; // 参战家族的信息
    private Map<Long, Long> roleId2FamilyIdMap;
    private Set<Long> fightingInBattleRoleIdMap; // 正在战斗中的roleId的哈希表


    /* 战斗管理 */
    private String eliteFightId;
    private FamilyWarEliteFight eliteFight;
    private Map<String, FamilyWarNormalFight> normalFightMap;
    private Map<String, Long> normalPersonalPoints;
    private Map<String, FamilyWarStageFight> stageFightMap;

    /* 匹配战每场奖励累计 */
    private Map<Long, Map<Integer, Integer>> normalFightAwardAccumulatedMap;

    private int screenings;//场次
    private long startFightTimeStamp = 0;//本场战斗开始时间
    private long lastEndFightTimeStamp = 0;//上一场战斗结束时间
    private boolean isFighting = false;  //是否正在战斗
    private boolean isEliteFinish = false;
    private String nextFightTimeStr = "";
    private int camp1MoraleCache;//阵营1士气缓存
    private int camp2MoraleCache;//阵营2士气缓存

    private Map<Integer, FamilyWarEliteFightStat> statEliteList;
    private List<FamilyWarNormalFightStat> statNormalList;
    /**
     * ========================================================================
     */
    private Map<Integer, FightStat> fightStatMap;

    public FamilyWarKnockoutBattle(String battleId, int type, KnockoutFamilyInfo camp1Info, KnockoutFamilyInfo camp2Info) {
        this.battleId = battleId;
        this.type = type;
        this.camp1FamilyId = camp1Info.getFamilyId();
        this.camp2FamilyId = camp2Info.getFamilyId();
        this.camp1MainServerId = camp1Info.getMainServerId();
        this.camp2MainServerId = camp2Info.getMainServerId();
        this.familyInfoMap = new HashMap<>();
        this.roleId2FamilyIdMap = new HashMap<>();
        this.fightingInBattleRoleIdMap = new HashSet<>();
        familyInfoMap.put(camp1FamilyId, createBattleCampInfo(1, camp1Info));
        familyInfoMap.put(camp2FamilyId, createBattleCampInfo(2, camp2Info));
        this.normalFightMap = new HashMap<>();
        this.stageFightMap = new HashMap<>();
        this.normalFightAwardAccumulatedMap = new HashMap<>();
        this.normalPersonalPoints = new ConcurrentHashMap<>();
        this.screenings = 0;
        this.camp1MoraleCache = 0;
        this.camp2MoraleCache = 0;
        statEliteList = new HashMap<>();
        statNormalList = new ArrayList<>();
    }

    public void printState() {
        LogUtil.info("容器大小输出:{},battleId:{},camp1FamilyId:{},camp2FamilyId:{},场次:{},eliteFightId:{},isFighting:{},isEliteFinish:{},roleId2FamilyId:{},fightingInBattleRoleId:{}",
                this.getClass().getSimpleName(), battleId, camp1FamilyId, camp2FamilyId, screenings, eliteFightId, isFighting, isEliteFinish, roleId2FamilyIdMap, fightingInBattleRoleIdMap);
    }

    public Set<Long> getFightingInBattleRoleIdMap() {
        return fightingInBattleRoleIdMap;
    }

    public String getBattleId() {
        return battleId;
    }

    public int getType() {
        return type;
    }

    public FightBaseService fightService() {
        return familyWar.fightService();
    }

    public RoleService roleService() {
        return familyWar.roleService();
    }

    public FamilyWar getFamilyWar() {
        return familyWar;
    }

    public void setFamilyWar(FamilyWar familyWar) {
        this.familyWar = familyWar;
    }

    public int getScreenings() {
        return screenings;
    }

    public boolean isEliteFinish() {
        return isEliteFinish;
    }

    public boolean isFighting() {
        return isFighting;
    }

    public long getStartFightTimeStamp() {
        return startFightTimeStamp;
    }

    public long getLastEndFightTimeStamp() {
        return lastEndFightTimeStamp;
    }

    public String getNextFightTimeStr() {
        return nextFightTimeStr;
    }

    public String getFamilyName(long familyId) {
        return familyInfoMap.get(familyId).familyName;
    }

    public Map<String, FighterEntity> campFighterMap(long familyId) {
        return toStringFighterEntityMap(familyInfoMap.get(familyId).eliteFighterMap);
    }

    public Map<Integer, FightStat> getFightStatMap() {
        return fightStatMap;
    }

    public long getFamilyFightScore(long familyId) {
        return familyInfoMap.get(familyId).totalFightScore;
    }

    public Map<Long, Long> getRoleId2FamilyIdMap() {
        return roleId2FamilyIdMap;
    }

    public int getBattleType() {
        return battleType;
    }

    public long getCampFightScore(long familyId) {
        return familyInfoMap.get(familyId).totalFightScore;
    }

    public final void start(int battleType) {
        screenings++;
        eliteFightId = battleType + "-" + MultiServerHelper.getServerId() + "-" + FightIdCreator.creatUUId();
        eliteFight = new FamilyWarEliteFight();
        int serverId = ServiceHelper.fsManagerService().getFightServer(FSManagerServiceActor.FIGHT_SERVER_LEVEL_COMM);
        LogUtil.info("familywar|fightServerId:{}", serverId);
        eliteFight.setFightServerId(serverId);
        eliteFight.setFightId(eliteFightId);
        eliteFight.setBattle(this);
        eliteFight.setCamp1FamilyId(camp1FamilyId);
        eliteFight.setCamp2FamilyId(camp2FamilyId);
        eliteFight.setCamp1FamilyName(familyInfoMap.get(camp1FamilyId).familyName);
        eliteFight.setCamp2FamilyName(familyInfoMap.get(camp2FamilyId).familyName);
        eliteFight.setCamp1MainServerId(camp1MainServerId);
        eliteFight.setCamp2MainServerId(camp2MainServerId);
        eliteFight.setCamp1FighterMap(toStringFighterEntityMap(familyInfoMap.get(camp1FamilyId).eliteFighterMap));
        eliteFight.setCamp2FighterMap(toStringFighterEntityMap(familyInfoMap.get(camp2FamilyId).eliteFighterMap));
        eliteFight.setCamp1TotalFightScore(familyInfoMap.get(camp1FamilyId).totalFightScore);
        eliteFight.setCamp2TotalFightScore(familyInfoMap.get(camp2FamilyId).totalFightScore);
        eliteFight.createFight(screenings, statEliteList, camp1MoraleCache, camp2MoraleCache, battleType);
        this.battleType = battleType;
        camp1MoraleCache = 0;
        camp2MoraleCache = 0;
        startFightTimeStamp = System.currentTimeMillis();
        lastEndFightTimeStamp = 0;
        isFighting = true;
        nextFightTimeStr = "";
        LogUtil.info("familywar|开始 {} 战斗:第{}场,camp1:{},camp2:{}", battleType, screenings, camp1FamilyId, camp2FamilyId);
        /**========================================================================*/
//        FamilyWarEliteBattle eliteBattle = new FamilyWarEliteBattle();
//        eliteBattle.setKnockoutBattle(this);
//        eliteBattle.onInitFight();
    }

    public final void end(boolean hasNext) {
        lastEndFightTimeStamp = System.currentTimeMillis();
        startFightTimeStamp = 0;
        isFighting = false;
        // 通知战斗服停止
        if (screenings >= FamilyActWarManager.battleCount) {
            hasNext = false;
            lastEndFightTimeStamp = 0;
        } else {
            long timeStr = lastEndFightTimeStamp + FamilyActWarManager.familywar_intervaltime * 1000;
            nextFightTimeStr = TimeUtil.toHHmmss(timeStr);
        }
        eliteFight.endFight(hasNext);
    }

    public final void end(String fightId, long winnerFamilyId, long loserFamilyId, FamilyWarEliteFightStat stat, boolean hasNext) {
        lastEndFightTimeStamp = System.currentTimeMillis();
        startFightTimeStamp = 0;
        isFighting = false;
        // 通知战斗服停止
        if (screenings >= FamilyActWarManager.battleCount) {
            hasNext = false;
            lastEndFightTimeStamp = 0;
        } else {
            long timeStr = lastEndFightTimeStamp + FamilyActWarManager.familywar_intervaltime * 1000;
            nextFightTimeStr = TimeUtil.toHHmmss(timeStr);
        }
        double camp1TowerTotalHp = 0;
        double camp2TowerTotalHp = 0;
        double camp1TowerHp = 0;
        double camp2TowerHp = 0;
        for (EliteFightTower tower : eliteFight.getTowerMap().values()) {
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
        LogUtil.info("familywar|battle结算时塔的血量 camp1Tower :{} ,camp1TowerHp:{} ,camp1TowerTotalHp:{}, camp2Tower:{}, " +
                "camp2TowerHp:{}, camp2TowerTotalHp:{}", camp1Tower, camp1TowerHp, camp1TowerTotalHp, camp2Tower, camp2TowerHp, camp2TowerTotalHp);
        stat.setCamp1TowerHp(camp1Tower);
        stat.setCamp2TowerHp(camp2Tower);
        finishEliteFight(fightId, winnerFamilyId, loserFamilyId, stat, hasNext, FamilyWarConst.WIN_BY_CRYSTAL_DEAD);
    }

    public final void enterEliteFight(int mainServerId, long familyId, long roleId, FighterEntity fighterEntity) {
        if (eliteFight == null) return;
        LogUtil.info("familywar|battle==家族:{}的玩家:{}进入精英战场", familyId, roleId);
        eliteFight.enter(mainServerId, familyId, roleId, fighterEntity);
        FamilyBattleInfo familyBattleInfo = familyInfoMap.get(familyId);
        if (familyBattleInfo == null) return;
        familyBattleInfo.eliteFighterMap.put(roleId, fighterEntity);
    }

    public final void onClientPreloadFinished(int mainServerId, String fightId, long roleId) {
        sendBattleFightInitInfo(mainServerId, roleId, false, fightId);
        if (fightId.equals(eliteFightId)) {
            eliteFight.syncPersonalPoints(Long.toString(roleId), statEliteList); // 精英战的个人积分
        } else {
            FamilyWarNormalFight normalFight = normalFightMap.get(fightId);
            if (normalFight == null) return;
            normalFight.syncPersonalPoints(Long.toString(roleId)); // 匹配战的个人积分
        }
    }

    public final void sendBattleFightInitInfo(int mainServerId, long roleId, boolean isEliteFight, String fightId) {
        if (fightId.equals(eliteFightId)) {
            if (eliteFight == null) {
                return;
            }
            ClientFamilyWarBattleFightInitInfo packet = new ClientFamilyWarBattleFightInitInfo(ClientFamilyWarBattleFightInitInfo.ELITE, eliteFight.getTowerMap());
            packet.setBattleType((byte) type);
            packet.setSubBattleType((byte) screenings);
            roleService().send(getMainServerId(roleId), roleId, packet);
            int camp1Morale = eliteFight.getStat().getCamp1Morale();
            int camp2Morale = eliteFight.getStat().getCamp2Morale();
            int camp1BuffId = eliteFight.getCamp1BuffId();
            int camp2BuffId = eliteFight.getCamp2BuffId();
            long camp1Points = 0;
            long camp2Points = 0;
            FamilyWarEliteFightStat stat = eliteFight.getStat();
            if (stat != null) {
                camp1Points = stat.getCamp1TotalPoints();
                camp2Points = stat.getCamp2TotalPoints();
            }
            double camp1TowerTotalHp = 0;
            double camp2TowerTotalHp = 0;
            double camp1TowerHp = 0;
            double camp2TowerHp = 0;
            for (EliteFightTower tower : eliteFight.getTowerMap().values()) {
                if (tower.getCamp() == K_CAMP1) {
                    camp1TowerTotalHp += tower.getMaxHp();
                    camp1TowerHp += tower.getHp();
                } else if (tower.getCamp() == K_CAMP2) {
                    camp2TowerTotalHp += tower.getMaxHp();
                    camp2TowerHp += tower.getHp();
                }
            }
            double camp1Tower = (camp1TowerHp / camp1TowerTotalHp) * 100;
            double camp2Tower = (camp2TowerHp / camp2TowerTotalHp) * 100;
            ClientFamilyWarBattleFightUpdateInfo packets = new ClientFamilyWarBattleFightUpdateInfo(ClientFamilyWarBattleFightInitInfo.ELITE,
                    camp1Morale, camp2Morale, camp1BuffId, camp2BuffId, camp1Points, camp2Points, camp1Tower, camp2Tower, eliteFight.getTowerMap());
            roleService().send(getMainServerId(roleId), roleId, packets);
            if (isEliteFight) {
                eliteFight.syncPersonalPoints(Long.toString(roleId), statEliteList);
            }
        } else {
            FamilyWarNormalFight normalFight = normalFightMap.get(fightId);
            if (normalFight == null) return;
            roleService().send(getMainServerId(roleId), roleId, new ClientFamilyWarBattleFightInitInfo(ClientFamilyWarBattleFightInitInfo.NORMAL, normalFight.getTowerMap()));

            double camp1TowerTotalHp = 0;
            double camp2TowerTotalHp = 0;
            double camp1TowerHp = 0;
            double camp2TowerHp = 0;
            for (EliteFightTower tower : normalFight.getTowerMap().values()) {
                if (tower.getCamp() == K_CAMP1) {
                    camp1TowerTotalHp += tower.getMaxHp();
                    camp1TowerHp += tower.getHp();
                } else if (tower.getCamp() == K_CAMP2) {
                    camp2TowerTotalHp += tower.getMaxHp();
                    camp2TowerHp += tower.getHp();
                }
            }
            double camp1Tower = (camp1TowerHp / camp1TowerTotalHp) * 100;
            double camp2Tower = (camp2TowerHp / camp2TowerTotalHp) * 100;
            roleService().send(getMainServerId(roleId), roleId, new ClientFamilyWarBattleFightUpdateInfo(ClientFamilyWarBattleFightInitInfo.NORMAL,
                    camp1Tower, camp2Tower, normalFight.getTowerMap()));
        }
    }

    public final void sendBattleFightUpdateInfo() {
        sendBattleFightUpdateInfo(eliteFightId);
        for (String fightId : normalFightMap.keySet()) {
            sendBattleFightUpdateInfo(fightId);
        }
    }

    public final void sendBattleFightUpdateInfo(String fightId) {
        if (fightId.equals(eliteFightId)) {
            if (eliteFight == null || isEliteFinish) return;
            int camp1Morale = eliteFight.getStat().getCamp1Morale();
            int camp2Morale = eliteFight.getStat().getCamp2Morale();
            int camp1BuffId = eliteFight.getCamp1BuffId();
            int camp2BuffId = eliteFight.getCamp2BuffId();
            long camp1Points = 0;
            long camp2Points = 0;
            FamilyWarEliteFightStat stat = eliteFight.getStat();
            if (stat != null) {
                camp1Points = stat.getCamp1TotalPoints();
                camp2Points = stat.getCamp2TotalPoints();
            }
            double camp1TowerTotalHp = 0;
            double camp2TowerTotalHp = 0;
            double camp1TowerHp = 0;
            double camp2TowerHp = 0;
            for (EliteFightTower tower : eliteFight.getTowerMap().values()) {
                if (tower.getCamp() == K_CAMP1) {
                    camp1TowerTotalHp += tower.getMaxHp();
                    camp1TowerHp += tower.getHp();
                } else if (tower.getCamp() == K_CAMP2) {
                    camp2TowerTotalHp += tower.getMaxHp();
                    camp2TowerHp += tower.getHp();
                }
            }
            double camp1Tower = (camp1TowerHp / camp1TowerTotalHp) * 100;
            double camp2Tower = (camp2TowerHp / camp2TowerTotalHp) * 100;
            ClientFamilyWarBattleFightUpdateInfo packet = new ClientFamilyWarBattleFightUpdateInfo(ClientFamilyWarBattleFightInitInfo.ELITE,
                    camp1Morale, camp2Morale, camp1BuffId, camp2BuffId, camp1Points, camp2Points, camp1Tower, camp2Tower, eliteFight.getTowerMap());
            List<Long> fightIds = eliteFight.getFightIds();
//            LogUtil.info("elite|sendBattleFightUpdateInfo|fightId:{},roleIds:{}", fightId, fightIds);
            for (long roleId : eliteFight.getFightIds()) {
                roleService().notice(getMainServerId(roleId), roleId, new FamilyWarSendPacketEvent(packet, FamilyWarConst.SCENE_TYPE_FAMILY_WAR));
            }
        } else {
            FamilyWarNormalFight normalFight = normalFightMap.get(fightId);
            if (normalFight == null) return;
            double camp1TowerTotalHp = 0;
            double camp2TowerTotalHp = 0;
            double camp1TowerHp = 0;
            double camp2TowerHp = 0;
            for (EliteFightTower tower : normalFight.getTowerMap().values()) {
                if (tower.getCamp() == K_CAMP1) {
                    camp1TowerTotalHp += tower.getMaxHp();
                    camp1TowerHp += tower.getHp();
                } else if (tower.getCamp() == K_CAMP2) {
                    camp2TowerTotalHp += tower.getMaxHp();
                    camp2TowerHp += tower.getHp();
                }
            }
            double camp1Tower = (camp1TowerHp / camp1TowerTotalHp) * 100;
            double camp2Tower = (camp2TowerHp / camp2TowerTotalHp) * 100;
            ClientFamilyWarBattleFightUpdateInfo packet = new ClientFamilyWarBattleFightUpdateInfo(ClientFamilyWarBattleFightInitInfo.NORMAL,
                    camp1Tower, camp2Tower, normalFight.getTowerMap());
            List<Long> fightIds = normalFight.getFightIds();
//            LogUtil.info("normal|sendBattleFightUpdateInfo|fightId:{},roleIds:{}", fightId, fightIds);
            for (long roleId : fightIds) {
                roleService().notice(getMainServerId(roleId), roleId, new FamilyWarSendPacketEvent(packet, FamilyWarConst.SCENE_TYPE_FAMILY_WAR));
            }
        }
    }

    /**
     * 进入匹配战场匹配队列
     *
     * @param controlServerId
     * @param mainServerId
     * @param familyId
     * @param roleId
     * @param fighterEntity
     */
    public final void enterNormalFightWaitingQueue(int controlServerId, int mainServerId, long familyId, long roleId, FighterEntity fighterEntity) {
        if (!(familyWar instanceof FamilyWarKnockout)){
            FamilyWarOnlinePlayerMap.roleOnline(roleId);
        }
        FamilyBattleInfo familyBattleInfo = familyInfoMap.get(familyId);
        familyBattleInfo.normalFighterMap.put(roleId, fighterEntity);
        /** ==============================================================================    */
        // fixme:另外还要加上状态判断（是否已经在比赛中）
        if (!checkFighterState(familyBattleInfo, roleId)) {
            for (FamilyWarNormalFight normalFight : normalFightMap.values()) {
                if (normalFight.inFight(roleId)) {
                    normalFight.enterNormalFight(mainServerId, familyId, roleId, fighterEntity);
                    LogUtil.info("familywar|普通场次|重复进入|roleId:{},familyId:{},fightId:{}", roleId, familyId, normalFight.getFightId());
                    return;
                }
            }
        }
        if (!familyBattleInfo.dummySet.contains(roleId)) {
            familyBattleInfo.waitingQueue.put(roleId, now());
            LogUtil.info("familywar|put to waitingQueue {},familyId:{}", roleId, familyId);
            familyBattleInfo.idleSet.remove(roleId);
        } else {
            familyBattleInfo.waitingForDummySet.add(roleId);
        }
        LogUtil.info("familywar|waitingQueue:{}", familyBattleInfo.waitingQueue);
        familyWar.roleService().send(mainServerId, roleId, new ClientFamilyWarUiEnter(ClientFamilyWarUiEnter.SUBTYPE_MATCHING)); // 通知客户端顺计时
    }

    /**
     * 退出匹配战场的匹配
     *
     * @param controlServerId
     * @param mainServerId
     * @param familyId
     * @param roleId
     */
    public final void cancelNormalFightWaitingQueue(int controlServerId, int mainServerId, long familyId, long roleId) {
        LogUtil.info("familywar|退出战场的匹配 roleId:{},familyId:{}", roleId, familyId);
        FamilyBattleInfo familyBattleInfo = familyInfoMap.get(familyId);
        boolean backCity = false;
        if (familyBattleInfo.waitingQueue.containsKey(roleId)) {
            familyBattleInfo.waitingQueue.remove(roleId);
            familyBattleInfo.idleSet.add(roleId);
            familyWar.roleService().send(mainServerId, roleId, new ClientFamilyWarUiEnter(ClientFamilyWarUiEnter.SUBTYPE_CANCEL_OK));
            backCity = true;
        } else if (familyBattleInfo.waitingForDummySet.contains(roleId)) {
            familyBattleInfo.waitingForDummySet.remove(roleId);
            familyWar.roleService().send(mainServerId, roleId, new ClientFamilyWarUiEnter(ClientFamilyWarUiEnter.SUBTYPE_CANCEL_OK));
            backCity = true;
        }
        if (backCity) {
            // 抛事件
            //knockout.enterSafeScene(controlServerId, mainServerId, familyId, roleId);
        }
    }

    public void onNormalFightCreationSucceeded(int mainServerId, int fightServerId, String fightId) {
        FamilyWarNormalFight fight = normalFightMap.get(fightId);
        fight.onFightCreationSucceeded();
    }

    public void onNormalFightStarted(int mainServerId, int fightServerId, String fightId) {
        FamilyWarNormalFight fight = normalFightMap.get(fightId);
        fight.setCreationTimestamp(System.currentTimeMillis());
    }

    public final void finishNormalFight(String fightId, long winnerFamilyId, long loserFamilyId, FamilyWarNormalFightStat stat, int finishType) {
        updateFamilyPoint(winnerFamilyId, FamilyActWarManager.familywar_pvpwinscore);
        updateMorale(winnerFamilyId, familywar_smallwinscore);
        updateMorale(loserFamilyId, familywar_smallfailscore);
        sendToAllFighter(winnerFamilyId);
        sendToAllFighter(loserFamilyId);
        LogUtil.info("familywar|普通场|淘汰赛[{}]，{}胜利，{}失败", battleId, winnerFamilyId, loserFamilyId);
        String winnerFamilyName = familyInfoMap.get(winnerFamilyId).familyName;
        String loserFamilyName = familyInfoMap.get(loserFamilyId).familyName;
        String winText = getWintext(finishType, winnerFamilyName, loserFamilyName);
        for (FamilyWarNormalFightPersonalStat personalStat : stat.getPersonalStatMap().values()) {
            long familyId = roleId2FamilyIdMap.get(personalStat.getFighterId());
            ClientFamilyWarFightNormalResult result = new ClientFamilyWarFightNormalResult();
            result.setStat(stat);
            result.setWinText(winText);
            result.setToolMap(stat.getPersonalToolMap().containsKey(personalStat.getFighterId()) ? stat.getPersonalToolMap().get(personalStat.getFighterId()) : new HashMap<Integer, Integer>());
            //toolMap
            if (familyId == camp1FamilyId) {
                roleService().notice(camp1MainServerId, personalStat.getFighterId(), new FamilyWarSendPacketEvent(result, FamilyWarConst.SCENE_TYPE_FAMILY_WAR));
            } else if (familyId == camp2FamilyId) {
                roleService().notice(camp2MainServerId, personalStat.getFighterId(), new FamilyWarSendPacketEvent(result, FamilyWarConst.SCENE_TYPE_FAMILY_WAR));
            } else {
                LogUtil.info(personalStat.getFighterId() + "familywar|" + "找不到家族，不能发送精英赛结算数据");
            }
        }
        removeNormalFight(fightId);
    }

    /**
     * 精英赛结束
     * fixme:综合所有比分在判决胜利失败
     *
     * @param fightId
     * @param winnerFamilyId
     * @param loserFamilyId
     * @param stat
     */
    public final void finishEliteFight(String fightId, long winnerFamilyId, long loserFamilyId, FamilyWarEliteFightStat stat, boolean hasNext, int finishType) {
        stat.setWinnerFamilyId(winnerFamilyId);
        stat.setLoserFamilyId(loserFamilyId);
        updateFamilyPoint(winnerFamilyId, FamilyActWarManager.familywar_elitewinscore);
        updateFamilyPoint(loserFamilyId, FamilyActWarManager.familywar_elitefailscore);
        sendToAllFighter(winnerFamilyId);
        sendToAllFighter(loserFamilyId);
        Map<Long, Map<Integer, Integer>> fighterAwardMap = new HashMap<>();
        LogUtil.info("familywar|淘汰赛[{}]，{}胜利，{}失败", battleId, winnerFamilyId, loserFamilyId);
        for (FamilyWarEliteFightPersonalStat personalStat : stat.getPersonalStatMap().values()) {
            long familyId = roleId2FamilyIdMap.get(personalStat.getFighterId());
            if (personalStat.getPoints() >= 0) { // fixme: gt 0
                if (familyId == winnerFamilyId) {
                    Map<Integer, Integer> toolMap = getToolMap(true);
                    fighterAwardMap.put(personalStat.getFighterId(), toolMap);
                    updateElitePoints(Long.toString(personalStat.getFighterId()), FamilyActWarManager.familywar_score_elitewin);
                    eliteFight.getStat().updatePersonalStat(this, getMainServerId(personalStat.getFighterId()),
                            personalStat.getFighterId(), 0, 0, 0, 0, FamilyActWarManager.familywar_score_elitewin, eliteFight.playOrAI(personalStat.getFighterId()));
                    logEvent(FamilyWarConst.eliteWarLog, FamilyWarConst.successLog, familyWar == null ? 0 : familyWar.getElitePointsRankList(personalStat.getFighterId()).getRank(Long.toString(personalStat.getFighterId())), personalStat.getPoints(),
                            personalStat.getKillCount(), personalStat.getFighterId(), battleType == FamilyWarConst.W_TYPE_LOCAL ? 1 : 2, battleType == FamilyWarConst.W_TYPE_QUALIFYING ? 1 : 2, toolMap);
                } else {
                    Map<Integer, Integer> toolMap = getToolMap(false);
                    fighterAwardMap.put(personalStat.getFighterId(), toolMap);
                    logEvent(FamilyWarConst.eliteWarLog, FamilyWarConst.failLog, familyWar == null ? 0 : familyWar.getElitePointsRankList(personalStat.getFighterId()).getRank(Long.toString(personalStat.getFighterId())), personalStat.getPoints(),
                            personalStat.getKillCount(), personalStat.getFighterId(), battleType == FamilyWarConst.W_TYPE_LOCAL ? 1 : 2, battleType == FamilyWarConst.W_TYPE_QUALIFYING ? 1 : 2, toolMap);
                }
            }
        }
        String camp1FamilyName = familyInfoMap.get(camp1FamilyId).familyName;
        String camp2FamilyName = familyInfoMap.get(camp2FamilyId).familyName;
        sendEliteFightAward(winnerFamilyId == camp1FamilyId, type, screenings, camp1FamilyName, camp2FamilyName, fighterAwardMap);
        sendEliteFightResult(stat, fighterAwardMap, finishType, familyInfoMap.get(winnerFamilyId).familyName,
                familyInfoMap.get(loserFamilyId).familyName);
        eliteFight.stopFight();
        // 三场结束战斗,这里需要将精英站玩家加入匹配队列
        if (!hasNext) {
            this.isEliteFinish = true;
            LogUtil.info("familywar|三场精英站结束:{}", isEliteFinish);
        } else {
            eliteFight = null;
        }
    }

    private Map<Integer, Integer> getToolMap(boolean win) {
        Map<Integer, Integer> toolMap;
        if (win && battleType == FamilyWarConst.W_TYPE_LOCAL) {
            toolMap = DropManager.executeDrop(dropIdOfEliteFightWinAward, 1);
        } else if (!win && battleType == FamilyWarConst.W_TYPE_LOCAL) {
            toolMap = DropManager.executeDrop(dropIdOfEliteFightLoseAward, 1);
        } else if (win && battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
            toolMap = DropManager.executeDrop(dropIdOfEliteQualifyFightWinAward, 1);
        } else if (!win && battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
            toolMap = DropManager.executeDrop(dropIdOfEliteQualifyFightLoseAward, 1);
        } else if (win && battleType == FamilyWarConst.W_TYPE_REMOTE) {
            toolMap = DropManager.executeDrop(dropIdOfEliteRemoteFightWinAward, 1);
        } else {
            toolMap = DropManager.executeDrop(dropIdOfEliteRemoteFightLoseAward, 1);
        }
        return toolMap;
    }

    public void logEvent(int type, int success, int rank, long points, int kill, long fighterId, int warType, int battle, Map<Integer, Integer> toolMap) {
        FamilyWarLogEvent logEvent = new FamilyWarLogEvent();
        logEvent.setType(type);
        logEvent.setSuccess(success);
        logEvent.setIntegral(points);
        logEvent.setKill(kill);
        logEvent.setRank(rank);
        logEvent.setWarType(warType);
        logEvent.setBattleType(battle);
        logEvent.setItemMap(toolMap);
        roleService().notice(getMainServerId(fighterId), fighterId, logEvent);
    }


    public void finishAllFight() {
        for (FamilyWarNormalFight normalFight : normalFightMap.values()) {
            normalFight.stopFight(false);
        }
        for (FamilyWarStageFight stageFight : stageFightMap.values()) {
            stageFight.stopFight(false);
        }
        sendNormalFightAward();
        // 取消匹配
        LogUtil.info("familywar| familyInfoMap:{}", familyInfoMap.keySet());
        for (FamilyBattleInfo familyBattleInfo : familyInfoMap.values()) {
            try {
                for (long roleId : familyBattleInfo.waitingQueue.keySet()) {
                    roleService().send(getMainServerId(roleId), roleId, new ClientFamilyWarUiEnter(ClientFamilyWarUiEnter.SUBTYPE_CANCEL_OK));
                }
                familyBattleInfo.waitingQueue = null;
                for (long roleId : familyBattleInfo.waitingForDummySet) {
                    roleService().send(getMainServerId(roleId), roleId, new ClientFamilyWarUiEnter(ClientFamilyWarUiEnter.SUBTYPE_CANCEL_OK));
                }
                familyBattleInfo.waitingForDummySet = null;
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
        eliteFight.endAllFight();
    }

    public void finishFightResult(long camp1TotalFightScore, long camp2TotalFightScore) {
        LogUtil.info("familywar|{}:比赛结束,结算界面", type);
        long family1Points = getFamilyPoints(camp1FamilyId);
        long family2Points = getFamilyPoints(camp2FamilyId);
        long winFamilyId;
        long losFamilyId;
        if (family1Points != family2Points) {
            winFamilyId = family1Points > family2Points ? camp1FamilyId : camp2FamilyId;
            losFamilyId = family1Points < family2Points ? camp1FamilyId : camp2FamilyId;
        } else if (camp1TotalFightScore != camp2TotalFightScore) {
            winFamilyId = camp1TotalFightScore > camp2TotalFightScore ? camp1FamilyId : camp2FamilyId;
            losFamilyId = camp1TotalFightScore < camp2TotalFightScore ? camp1FamilyId : camp2FamilyId;
        } else {
            winFamilyId = camp1FamilyId > camp2FamilyId ? camp1FamilyId : camp2FamilyId;
            losFamilyId = camp1FamilyId < camp2FamilyId ? camp1FamilyId : camp2FamilyId;
        }
        LogUtil.info("精英赛|家族成员:{},winFamilyId:{},losFamilyId:{}", roleId2FamilyIdMap, winFamilyId, losFamilyId);
        for (Entry<Long, Long> entry : roleId2FamilyIdMap.entrySet()) {
            long enemyFamilyId;
            byte result = 0;
            if (entry.getValue() == winFamilyId) {
                result = 1;
                enemyFamilyId = losFamilyId;
            } else {
                enemyFamilyId = winFamilyId;
            }
            ClientFamilyWarBattleResult packet = new ClientFamilyWarBattleResult();
            packet.setBattleType(type);
            packet.setResult(result);
            packet.setMyFamilyName(familyInfoMap.get(entry.getValue()).familyName);
            packet.setEnemyFamilyName(familyInfoMap.get(enemyFamilyId).familyName);
            packet.setMyServerName(MultiServerHelper.getServerName((entry.getValue() == camp1FamilyId) ? camp1MainServerId : camp2MainServerId));
            packet.setEnemyServerName(MultiServerHelper.getServerName((enemyFamilyId == camp1FamilyId) ? camp1MainServerId : camp2MainServerId));
            packet.setMyEliteWinCount((byte) getWinEliteCount(entry.getValue()));
            packet.setEnemyEliteWinCount((byte) getWinEliteCount(enemyFamilyId));
            packet.setMyMatchWinCount(getWinNormalCount(entry.getValue()));
            packet.setEnemyMatchWinCount(getWinNormalCount(enemyFamilyId));
            packet.setMyKillCount(getKillCount(entry.getValue()));
            packet.setEnemyKillCount(getKillCount(enemyFamilyId));
            packet.setMyFamilyPoints(entry.getValue() == camp1FamilyId ? family1Points : family2Points);
            packet.setEnemyFamilyPoints(enemyFamilyId == camp1FamilyId ? family1Points : family2Points);
            packet.setNextBattleResultTime(FamilyWarUtil.getNextBattleTimeL(getActId()));
            packet.setWarType(battleType);
            roleService().notice(getMainServerId(entry.getKey()), entry.getKey(), new FamilyWarSendPacketEvent(packet, FamilyWarConst.SCENE_TYPE_NOT_FIGHT_SCENE));
        }
        familyWar.finishBattle(battleId, winFamilyId, losFamilyId);
    }

    private int getActId() {
        int actId = ActConst.ID_FAMILY_WAR_LOCAL;
        switch (battleType) {
            case FamilyWarConst.W_TYPE_LOCAL:
                actId = ActConst.ID_FAMILY_WAR_LOCAL;
                break;
            case FamilyWarConst.W_TYPE_QUALIFYING:
                actId = ActConst.ID_FAMILY_WAR_QUALIFYING;
                break;
            case FamilyWarConst.W_TYPE_REMOTE:
                actId = ActConst.ID_FAMILY_WAR_REMOTE;
                break;
        }
        LogUtil.info("familywar|获取当前活动Id:{},battleType:{}", actId, battleType);
        return actId;
    }

    private int getWinNormalCount(long familyId) {
        int winCount = 0;
        for (FamilyWarNormalFightStat stat : statNormalList) {
            if (stat.getWinnerFamilyId() == familyId) {
                winCount++;
            }
        }
        return winCount;
    }

    private int getWinEliteCount(long familyId) {
        int winCount = 0;
        for (FamilyWarEliteFightStat stat : statEliteList.values()) {
            if (stat.getWinnerFamilyId() == familyId) {
                winCount++;
            }
        }
        return winCount;
    }

    private int getKillCount(long familyId) {
        int killCount = 0;
        for (FamilyWarEliteFightStat stat : statEliteList.values()) {
            for (FamilyWarEliteFightPersonalStat personalStat : stat.getPersonalStatMap().values()) {
                if (roleId2FamilyIdMap.get(personalStat.getFighterId()) == familyId) {
                    killCount += personalStat.getKillCount();
                }
            }
        }
        return killCount;
    }

    /**
     * 累积3场精英战积分，外加上匹配战的pvp和pve积分
     *
     * @param familyId
     * @return
     */
    private int getFamilyPoints(long familyId) {
        int points = 0;
        for (FamilyWarEliteFightStat stat : statEliteList.values()) {
            if (familyId == stat.getCamp1FamilyId()) {
                points += stat.getCamp1TotalPoints();
            } else if (familyId == stat.getCamp2FamilyId()) {
                points += stat.getCamp2TotalPoints();
            }
        }
        return points;
    }

    public void updateFamilyPoint(long familyId, long points) {
        if (eliteFight == null) return;
        LogUtil.info("familywar|更新家族:{}的家族积分:{}", familyId, points);
        eliteFight.updateFamilyPoint(familyId, points);
    }

    public void sendToAllFighter(long familyId) {
        ClientFamilyWarBattleFamilyPoints packet = new ClientFamilyWarBattleFamilyPoints();
        for (FamilyWarEliteFightStat stat : statEliteList.values()) {
            if (familyId == camp1FamilyId) {
                packet.addMyFamilyPoints(stat.getCamp1TotalPoints());
                packet.addEnemyFamilyPoints(stat.getCamp2TotalPoints());
            } else {
                packet.addMyFamilyPoints(stat.getCamp2TotalPoints());
                packet.addEnemyFamilyPoints(stat.getCamp1TotalPoints());
            }
        }
        sendPointsPacket(familyId, packet);
    }

    public void sendToAllFighter(long familyId, boolean isStage) {
        if (familyId == camp1FamilyId) {
            sendToAllFighter(familyId);
            sendToAllFighter(camp2FamilyId);
        } else {
            sendToAllFighter(familyId);
            sendToAllFighter(camp1FamilyId);
        }
    }

    private void sendPointsPacket(long familyId, ClientFamilyWarBattleFamilyPoints packet) {
        FamilyBattleInfo info = familyInfoMap.get(familyId);
        if (info == null) return;
        LogUtil.info("familywar|下发积分改变给家族:{} 精英成员:[{}]|普通成员:[{}]", familyId, info.eliteFighterMap.keySet(), info.normalFighterMap.keySet());
        for (Long fightId : info.eliteFighterMap.keySet()) {
            roleService().send(getMainServerId(fightId), fightId, packet);
        }
        for (Long fightId : info.normalFighterMap.keySet()) {
            roleService().send(getMainServerId(fightId), fightId, packet);
        }
    }

    /**
     * 精英赛发奖
     *
     * @param isCamp1Win
     * @param camp1FamilyName
     * @param camp2FamilyName
     * @param fighterAwardMap
     */
    public final void sendEliteFightAward(boolean isCamp1Win, int type, int count, String camp1FamilyName, String camp2FamilyName, Map<Long, Map<Integer, Integer>> fighterAwardMap) {
        Map<Long, Map<Integer, Integer>> camp1FighterAwardMap = new HashMap<>();
        Map<Long, Map<Integer, Integer>> camp2FighterAwardMap = new HashMap<>();
        for (Entry<Long, Map<Integer, Integer>> entry : fighterAwardMap.entrySet()) {
            long familyId = roleId2FamilyIdMap.get(entry.getKey());
            if (familyId == camp1FamilyId) {
                camp1FighterAwardMap.put(entry.getKey(), entry.getValue());
            } else if (familyId == camp2FamilyId) {
                camp2FighterAwardMap.put(entry.getKey(), entry.getValue());
            }
        }
        if (familyWar instanceof FamilyWarKnockout) {
            ServiceHelper.familyWarLocalService().sendEliteFightAward(camp1MainServerId, isCamp1Win, type, count, camp2FamilyName, camp1FighterAwardMap);
            ServiceHelper.familyWarLocalService().sendEliteFightAward(camp2MainServerId, !isCamp1Win, type, count, camp1FamilyName, camp2FighterAwardMap);
        } else if (familyWar instanceof FamilyWarQualifying) {
            ServiceHelper.familyWarQualifyingService().sendEliteFightAward(FamilyWarUtil.getFamilyWarServerId(), camp1MainServerId, isCamp1Win, type, count, camp2FamilyName, camp1FighterAwardMap);
            ServiceHelper.familyWarQualifyingService().sendEliteFightAward(FamilyWarUtil.getFamilyWarServerId(), camp2MainServerId, !isCamp1Win, type, count, camp1FamilyName, camp2FighterAwardMap);
        } else if (familyWar instanceof FamilyWarRemote) {
            ServiceHelper.familyWarRemoteService().sendEliteFightAward(FamilyWarUtil.getFamilyWarServerId(), camp1MainServerId, isCamp1Win, type, count, camp2FamilyName, camp1FighterAwardMap);
            ServiceHelper.familyWarRemoteService().sendEliteFightAward(FamilyWarUtil.getFamilyWarServerId(), camp2MainServerId, !isCamp1Win, type, count, camp1FamilyName, camp2FighterAwardMap);
        }
    }

    /**
     * 匹配战发奖
     */
    public final void sendNormalFightAward() {
        Map<Long, Map<Integer, Integer>> camp1FighterAwardMap = new HashMap<>();
        Map<Long, Map<Integer, Integer>> camp2FighterAwardMap = new HashMap<>();
        for (Entry<Long, Map<Integer, Integer>> entry : normalFightAwardAccumulatedMap.entrySet()) {
            long familyId = roleId2FamilyIdMap.get(entry.getKey());
            long roleId = entry.getKey();
            if (!familyInfoMap.get(familyId).eliteFighterMap.containsKey(roleId)) {
                if (familyId == camp1FamilyId) {
                    camp1FighterAwardMap.put(entry.getKey(), entry.getValue());
                } else if (familyId == camp2FamilyId) {
                    camp2FighterAwardMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
        if (familyWar instanceof FamilyWarKnockout) {
            ServiceHelper.familyWarLocalService().sendNormalFightAward(camp1MainServerId, camp1FighterAwardMap);
            ServiceHelper.familyWarLocalService().sendNormalFightAward(camp2MainServerId, camp2FighterAwardMap);
        } else if (familyWar instanceof FamilyWarQualifying) {
            ServiceHelper.familyWarQualifyingService().sendNormalFightAward(FamilyWarUtil.getFamilyWarServerId(), camp1MainServerId, camp1FighterAwardMap);
            ServiceHelper.familyWarQualifyingService().sendNormalFightAward(FamilyWarUtil.getFamilyWarServerId(), camp2MainServerId, camp2FighterAwardMap);
        } else if (familyWar instanceof FamilyWarRemote) {
            ServiceHelper.familyWarRemoteService().sendNormalFightAward(FamilyWarUtil.getFamilyWarServerId(), camp1MainServerId, camp1FighterAwardMap);
            ServiceHelper.familyWarRemoteService().sendNormalFightAward(FamilyWarUtil.getFamilyWarServerId(), camp2MainServerId, camp2FighterAwardMap);
        }
    }

    /**
     * 发送精英战结算界面
     *
     * @param stat
     * @param fighterAwardMap
     */
    public final void sendEliteFightResult(FamilyWarEliteFightStat stat, Map<Long, Map<Integer, Integer>>
            fighterAwardMap, int finishType, String winnerFamilyName, String loserFamilyName) {
        String nonextround = DataManager.getGametext("familywar_tips_nonextround");
        String winnextround = String.format(DataManager.getGametext("familywar_tips_winnextround"), FamilyActWarManager.familywar_intervaltime);
        String losenextround = String.format(DataManager.getGametext("familywar_tips_losenextround"), FamilyActWarManager.familywar_intervaltime);
        LogUtil.info("nonextround:{}|winnextround:{}|losenextround:{}", nonextround, winnextround, losenextround);
        String winText = getWintext(finishType, winnerFamilyName, loserFamilyName);
        boolean isFinish = false;
        if (screenings >= FamilyActWarManager.battleCount) {
            isFinish = true;
        }
        for (Entry<Long, Map<Integer, Integer>> entry : fighterAwardMap.entrySet()) {
            long familyId = roleId2FamilyIdMap.get(entry.getKey());
            ClientFamilyWarBattleFightEliteResult packet = new ClientFamilyWarBattleFightEliteResult(stat, entry.getValue());
            packet.setCamp1ServerName(MultiServerHelper.getServerName(camp1MainServerId));
            packet.setCamp2ServerName(MultiServerHelper.getServerName(camp2MainServerId));
            packet.setWinText(winText);
            if (familyId == stat.getWinnerFamilyId()) {
                LogUtil.info("单场胜利:{}", winnextround);
                packet.setText(winnextround);
            }
            if (familyId == stat.getLoserFamilyId()) {
                LogUtil.info("单场失败:{}", winnextround);
                packet.setText(losenextround);
            }
            if (isFinish) {
                LogUtil.info("三场结束:{}", nonextround);
                packet.setText(nonextround);
            }
            if (familyId == camp1FamilyId) {
                roleService().notice(camp1MainServerId, entry.getKey(), new FamilyWarSendPacketEvent(packet, FamilyWarConst.SCENE_TYPE_FAMILY_WAR));
            } else if (familyId == camp2FamilyId) {
                roleService().notice(camp2MainServerId, entry.getKey(), new FamilyWarSendPacketEvent(packet, FamilyWarConst.SCENE_TYPE_FAMILY_WAR));
            } else {
                LogUtil.info(entry.getKey() + "familywar|" + "找不到家族，不能发送精英赛结算数据");
            }
        }
    }

    private String getWintext(int finishType, String winnerFamilyName, String loserFamilyName) {
        String winText = "";
        if (finishType == FamilyWarConst.WIN_BY_CRYSTAL_DEAD) {
            winText = String.format(DataManager.getGametext("familywar_desc_baseend"), loserFamilyName);
        } else if (finishType == FamilyWarConst.WIN_BY_TOWER_HP) {
            winText = String.format(DataManager.getGametext("familywar_desc_towerend"), winnerFamilyName);
        } else if (finishType == FamilyWarConst.WIN_BY_CAMP_MORALE) {
            winText = String.format(DataManager.getGametext("familywar_desc_moraleend"), winnerFamilyName);
        } else if (finishType == FamilyWarConst.WIN_BY_CAMP_FIGHTSCORE) {
            winText = String.format(DataManager.getGametext("familywar_desc_fightend"), winnerFamilyName);
        } else if (finishType == FamilyWarConst.WIN_BY_RANDOM) {
            winText = String.format(DataManager.getGametext("familywar_desc_randomend"), winnerFamilyName);
        }
        return winText;
    }

    public final void handleFighterQuit(long roleId, String fightId) {
        fightingInBattleRoleIdMap.remove(roleId);
        if (fightId.equals(eliteFightId)) {
            if (eliteFight != null && !isEliteFinish) {
                eliteFight.changePlayerState(roleId, FamilyWarConst.AI);
            }
        } else {
            FamilyWarNormalFight normalFight = normalFightMap.get(fightId);
            if (normalFight != null) {
                normalFight.changePlayerState(roleId, FamilyWarConst.AI);
            }

        }
    }

    public final void handleFighterEnter(long roleId, String fightId) {
        fightingInBattleRoleIdMap.add(roleId);
        LogUtil.info("familywar|add player[{}] into fightingInBattleRoleIdMap={}", roleId, fightingInBattleRoleIdMap);
        if (fightId.equals(eliteFightId) && eliteFight != null && !isEliteFinish) {
            eliteFight.changePlayerState(roleId, FamilyWarConst.play);
            return;
        }
        FamilyWarNormalFight normalFight = normalFightMap.get(fightId);
        if (normalFight != null) {
            normalFight.handlePlayerRevive(roleId, FamilyWarConst.play);
        }
    }

    public final void handleDamage(String fightId, Map<String, HashMap<String, Integer>> damage) {
        if (fightId.equals(eliteFightId) && eliteFight != null && !isEliteFinish) {
            eliteFight.handleDamage(damage);
        } else if (normalFightMap.containsKey(fightId)) {
            FamilyWarNormalFight normalFight = normalFightMap.get(fightId);
            if (normalFight != null) {
                normalFight.handleDamage(damage);
            } else {
                // todo: log it
            }
        }
    }

    public final void handleRevive(String fightId, String fighterUid, byte reqType) {
        if (fightId.equals(eliteFightId) && eliteFight != null && !isEliteFinish) {
            eliteFight.revive(fighterUid, reqType);
            return;
        }
        FamilyWarNormalFight normalFight = normalFightMap.get(fightId);
        if (normalFight != null) {
            normalFight.revive(fighterUid, reqType);
        }
    }

    public void handleRevive(String fightId, String fighterUid) {
        if (fightId.equals(eliteFightId) && eliteFight != null && !isEliteFinish) {
            eliteFight.handleRevive(fighterUid);
            return;
        }
        FamilyWarNormalFight normalFight = normalFightMap.get(fightId);
        if (normalFight != null) {
            normalFight.handleRevive(fighterUid);
        }
    }

    public final void handleDead(String fightId, Map<String, String> deadMap) {
        if (fightId.equals(eliteFightId) && eliteFight != null && !isEliteFinish) {
            eliteFight.handleDead(deadMap);
        } else if (normalFightMap.containsKey(fightId)) {
            FamilyWarNormalFight normalFight = normalFightMap.get(fightId);
            if (normalFight != null) {
                normalFight.handleDead(deadMap);
            } else {
                // todo: log it
            }
        } else {
            FamilyWarStageFight stageFight = stageFightMap.get(fightId);
            if (stageFight != null) {
                stageFight.handleDead(deadMap);
            } else {
                // todo: log it
            }
        }
    }

    public final void updateMorale(long familyId, int moraleDelta) {
        if (eliteFight == null) return;
        FamilyBattleInfo info = familyInfoMap.get(familyId);
        int curMorale = info.morale + moraleDelta;
        info.morale = curMorale; // 设置士气
        if (isFighting) {
            eliteFight.updateStatMorale(familyId, moraleDelta);
        } else {
            if (familyId == camp1FamilyId) {
                this.camp1MoraleCache += moraleDelta;
            } else {
                this.camp2MoraleCache += moraleDelta;
            }
        }
    }

    public void sendUpdateBuffInfo(long familyId, int preMorale, int curMorale) {
        if (eliteFight == null) return;
        FamilyWarMoraleVo preMoraleVo = getMoraleVo(preMorale);
        FamilyWarMoraleVo curMoraleVo = getMoraleVo(curMorale);
        LogUtil.info("familywar|curMoraleVo:{}|preMoraleVo == null || preMoraleVo != curMoraleVo:{}", curMoraleVo != null, preMoraleVo == null || preMoraleVo != curMoraleVo);
        if (curMoraleVo != null) {
            LogUtil.info("familywar|familyId:{}curMorale:{}|buffId:{}|deBuffId:{}", familyId, curMorale, curMoraleVo.getBuffId(), curMoraleVo.getDebuffId());
            if (preMoraleVo == null || preMoraleVo != curMoraleVo) { // 理应比较id的，但暂时没有id
                eliteFight.setBuff(familyId, curMoraleVo.getBuffId(), curMoraleVo.getDebuffId(), 1);
            }
        }
        sendBattleFightUpdateInfo(eliteFightId); // 同步
    }

    public final void updateElitePoints(String fighterUid, long delta) {
        // todo: 往上再调用
        familyWar.updateElitePoints(fighterUid, delta);
    }

    public final void updateNormalPoints(String fighterUid, long delta) {
        if (familyWar.getFamilyMap().get(roleId2FamilyIdMap.get(Long.parseLong(fighterUid))).getTeamSheet().contains(Long.parseLong(fighterUid)))
            return;
        familyWar.updateNormalPoints(fighterUid, delta);
        Long points = normalPersonalPoints.get(fighterUid);
        if (points == null) {
            points = 0L;
        }
        points = points + delta;
        normalPersonalPoints.put(fighterUid, points);
    }

    public long getElitePoint(String fighterUid) {
        return familyWar.getElitePoints(fighterUid);
    }

    /**
     * 获得Battle匹配赛积分
     *
     * @param fighterUid
     * @return
     */
    public long getBattleNormalPersonalPosints(String fighterUid) {
        Long points = normalPersonalPoints.get(fighterUid);
        return points == null ? 0L : points.longValue();
    }

    private Map<String, FighterEntity> toStringFighterEntityMap(Map<Long, FighterEntity> srcMap) {
        Map<String, FighterEntity> retMap = new HashMap<>();
        for (Entry<Long, FighterEntity> entry : srcMap.entrySet()) {
            retMap.put(Long.toString(entry.getKey()), entry.getValue());
        }
        return retMap;
    }

    public final void match(int battleType) {
        FamilyBattleInfo camp1Info = familyInfoMap.get(camp1FamilyId);
        FamilyBattleInfo camp2Info = familyInfoMap.get(camp2FamilyId);
        LogUtil.info("familywar|开始匹配,camp1:{},camp2:{},camp1WaitingQueue:{},camp2WaitingQueue:{}", camp1FamilyId, camp2FamilyId, camp1Info.waitingQueue.size(), camp2Info.waitingQueue.size());
        while (camp1Info.waitingQueue.size() > 0 || camp2Info.waitingQueue.size() > 0) {
            LogUtil.info("familywar|匹配中,camp1WaitingQueue:{},camp2WaitingQueue:{}", camp1Info.waitingQueue, camp2Info.waitingQueue);
            List<Long> camp1FighterList = null, camp2FighterList = null;
            // 生成阵营1的参赛名单
            if (camp1Info.waitingQueue.size() >= numOfFighterInNormalFight) {
                camp1FighterList = pollFromQueue(camp1Info.waitingQueue, numOfFighterInNormalFight);
            } else {
                camp1FighterList = pollFromQueue(camp1Info.waitingQueue, camp1Info.waitingQueue.size());
            }
            // 生成阵营2的参赛名单
            if (camp2Info.waitingQueue.size() >= numOfFighterInNormalFight) {
                camp2FighterList = pollFromQueue(camp2Info.waitingQueue, numOfFighterInNormalFight);
            } else {
                camp2FighterList = pollFromQueue(camp2Info.waitingQueue, camp2Info.waitingQueue.size());
            }
            // 设置阵营和出生点
            StageinfoVo stageInfoVo = SceneManager.getStageVo(stageIdOfNormalFight);
//            int idxOfCamp1Pos = 0;
            Map<String, FighterEntity> camp1FighterEntityMap = fillFighterEntity(camp1FamilyId, camp1FighterList, numOfFighterInNormalFight);
            for (FighterEntity entity : camp1FighterEntityMap.values()) {
                entity.setCamp(FamilyWarConst.K_CAMP1); // 设置阵营
                entity.setPosition(stageInfoVo.getPosition()); // 设置出生点
                entity.setRotation(stageInfoVo.getRotation());
//                idxOfCamp1Pos++;
            }
//            int idxOfCamp2Pos = 3;
            Map<String, FighterEntity> camp2FighterEntityMap = fillFighterEntity(camp2FamilyId, camp2FighterList, numOfFighterInNormalFight);
            for (FighterEntity entity : camp2FighterEntityMap.values()) {
                entity.setCamp(FamilyWarConst.K_CAMP2); // 设置阵营
                entity.setPosition(stageInfoVo.getEnemyPos(0)); // 设置出生点
                entity.setRotation(stageInfoVo.getEnemyRot(0));
//                idxOfCamp2Pos++;
            }
            for (FighterEntity entity : camp1FighterEntityMap.values()) {
                LogUtil.info("familywar|camp1RoleId:{},fighterType:{},camp:{}", entity.getUniqueId(), entity.getFighterType(), entity.getCamp());
            }
            for (FighterEntity entity : camp2FighterEntityMap.values()) {
                LogUtil.info("familywar|camp2RoleId:{},fighterType:{},camp:{}", entity.getUniqueId(), entity.getFighterType(), entity.getCamp());
            }
            LogUtil.info("camp1Size:{},camp2Size:{}", StringUtil.isNotEmpty(camp1FighterEntityMap), StringUtil.isNotEmpty(camp2FighterEntityMap));
            startNormalFight(camp1FighterEntityMap, camp2FighterEntityMap, battleType);
//            if (StringUtil.isNotEmpty(camp1FighterEntityMap) && StringUtil.isNotEmpty(camp2FighterEntityMap)) {
//                int random = RandomUtil.rand(0, 100);//如果是0就pvp，1就pve
//                LogUtil.info("familywar|随机值random:{},odd:{}", random, familywar_pairstageodd);
//                if (random > familywar_pairstageodd) {
//                    LogUtil.info("familywar|随机进入pve战场 camp1:{},camp2:{}", camp1FighterEntityMap.keySet(), camp2FighterEntityMap.keySet());
//                    startTeamStageFight(camp1FighterEntityMap, FamilyWarConst.K_CAMP1, camp1FamilyId, camp1MainServerId, battleType);
//                    startTeamStageFight(camp2FighterEntityMap, FamilyWarConst.K_CAMP2, camp2FamilyId, camp2MainServerId, battleType);
//                } else {
//                    LogUtil.info("familywar|随机进入pvp战场 camp1:{},camp2:{}", camp1FighterEntityMap.keySet(), camp2FighterEntityMap.keySet());
//                    startNormalFight(camp1FighterEntityMap, camp2FighterEntityMap, battleType);
//                }
//            } else if (StringUtil.isNotEmpty(camp1FighterEntityMap)) {
//                LogUtil.info("familywar|阵营2没有人，进入pve战场 camp1:{}", camp1FighterEntityMap);
//                startTeamStageFight(camp1FighterEntityMap, FamilyWarConst.K_CAMP1, camp1FamilyId, camp1MainServerId, battleType);
//            } else if (StringUtil.isNotEmpty(camp2FighterEntityMap)) {
//                LogUtil.info("familywar|阵营1没有人，进入pve战场 camp2:{}", camp1FighterEntityMap);
//                startTeamStageFight(camp2FighterEntityMap, FamilyWarConst.K_CAMP2, camp2FamilyId, camp2MainServerId, battleType);
//            }

        }
    }

    /**
     * 开始关卡战斗
     *
     * @param
     */
    public void startTeamStageFight(Map<String, FighterEntity> fighterEntityMap, byte camp, long familyId, int serverId, int battleType) {
        // 把机器人过滤掉
        StageinfoVo stageInfoVo = SceneManager.getStageVo(stageIdOfStageFight);
        int idxOfCamp1Pos = 0;
        Map<String, FighterEntity> fighterMap = new HashMap<>();
        Set<String> teamSheetSet = new HashSet<>();
        teamSheetSet.addAll(getTeamSheet(fighterEntityMap.keySet()));
        FamilyBattleInfo campInfo = familyInfoMap.get(familyId);
        for (Entry<String, FighterEntity> entry : fighterEntityMap.entrySet()) {
            if (FamilyWarUtil.isRobot(entry.getValue())) {
                updateFighterState(campInfo, Long.parseLong(entry.getKey()), BattleFighterState.IDLE, 0L);
                // 移除傀儡
                campInfo.dummySet.remove(Long.parseLong(entry.getKey()));
                campInfo.idleSet.add(Long.parseLong(entry.getKey()));
                continue;
            }
            entry.getValue().setPosition(stageInfoVo.getEnemyPos(idxOfCamp1Pos++));
            fighterMap.put(entry.getKey(), entry.getValue());
        }
        // 生成fightId
        String fightId = "fw-" + battleId + "-s-" + FightIdCreator.creatUUId(); // fixme: generate a new one
        if (StringUtil.isEmpty(fighterMap)) return;
        FamilyWarStageFight stageFight = new FamilyWarStageFight(fightId, familyId, serverId, fighterMap, this, teamSheetSet);
        stageFightMap.put(fightId, stageFight);
        stageFight.createFight(camp, battleType);
    }

    public Map<String, FighterEntity> getStageFighterEntities(String fightId) {
        FamilyWarStageFight stageFight = stageFightMap.get(fightId);
        if (stageFight != null) {
            return stageFight.getFighterEntities();
        }
        return null;
    }

    private Set<String> getTeamSheet(Set<String> entitySet) {
        Set<String> teamSheetSet = new HashSet<>();
        for (String fighterUid : entitySet) {
            if (familyWar.getFamilyMap().get(roleId2FamilyIdMap.get(Long.parseLong(fighterUid))).getTeamSheet().contains(Long
                    .parseLong(fighterUid))) {
                teamSheetSet.add(fighterUid);
            }
        }
        return teamSheetSet;
    }

    /**
     * 开始匹配战
     */
    public void startNormalFight(Map<String, FighterEntity> camp1FighterEntityMap, Map<String, FighterEntity>
            camp2FighterEntityMap, int battleType) {
        // 生成fightId
        String fightId = "fw-" + battleId + "-n-" + FightIdCreator.creatUUId(); // fixme: generate a new one
        Set<String> teamSheetSet = new HashSet<>();
        teamSheetSet.addAll(getTeamSheet(camp1FighterEntityMap.keySet()));
        teamSheetSet.addAll(getTeamSheet(camp2FighterEntityMap.keySet()));
        FamilyWarNormalFight normalFight = new FamilyWarNormalFight(fightId, camp1FamilyId,
                familyInfoMap.get(camp1FamilyId).familyName, camp2FamilyId, familyInfoMap.get(camp2FamilyId).familyName,
                camp1MainServerId, camp2MainServerId, camp1FighterEntityMap, camp2FighterEntityMap, this, teamSheetSet);
        normalFightMap.put(fightId, normalFight);
        normalFight.start(statNormalList, battleType);
    }

    public void removeBattle() {
        familyWar.removeBattle(battleId, type);
    }

    public void accumulateNormalFightAward(long roleId, Map<Integer, Integer> toolMap) {
        Map<Integer, Integer> accumulatedMap = normalFightAwardAccumulatedMap.get(roleId);
        if (accumulatedMap == null) {
            normalFightAwardAccumulatedMap.put(roleId, accumulatedMap = new HashMap<>());
        }
        MapUtil.add(accumulatedMap, toolMap);
    }

    public final Map<String, FighterEntity> fillFighterEntity(long campFamilyId, List<Long> campFighterIdList, int size) {
        Map<String, FighterEntity> entityMap = new HashMap<>();
        FamilyBattleInfo campInfo = familyInfoMap.get(campFamilyId);
        Set<Long> idleSetList = new HashSet<>();
        LogUtil.info("familywar|campFamilyId:{},waitQueue:{},idleSet:{},idleSetSize:{},eliteSheet:{},normalSheet:{}", campFamilyId, campInfo.waitingQueue, campInfo.idleSet, campInfo.idleSet.size(), campInfo.eliteFighterMap.keySet(), campInfo.normalFighterMap.keySet());
        while (entityMap.size() < size) {
            if (campFighterIdList.size() > 0) {
                long fighterId = campFighterIdList.remove(0);
                if (!checkFighterState(campInfo, fighterId)) {
                    LogUtil.info("familywar|该玩家还在战斗中:{}", fighterId);
                    continue;
                }
                FighterEntity entity = campInfo.normalFighterMap.get(fighterId);
                entity.setFighterType(FighterEntity.TYPE_PLAYER);
//                entity.setExtraValue("isAuto=1");// TODO: 2017-08-17 进入后设置为ai，后续再关闭
                updateFighterState(campInfo, fighterId, BattleFighterState.FIGHTING, System.currentTimeMillis());
                entityMap.put(toString(fighterId), entity);
            } else if (campInfo.idleSet.size() > 0) {
                long fighterId = SetUtil.randomAndRemoveElem(campInfo.idleSet);
                if (isOnline(fighterId)) {
                    idleSetList.add(fighterId);
                    LogUtil.info("在线的玩家，不再被动匹配 | roleId: {} ", fighterId);
                    continue;
                }
                FighterEntity entity = campInfo.normalFighterMap.get(fighterId);
                entity.setFighterType(FighterEntity.TYPE_PLAYER);// TODO: 2017-08-09 改成玩家，后续再设置为AI
                entity.setExtraValue("isAuto=1");
                updateFighterState(campInfo, fighterId, BattleFighterState.AI_FIGHTING, System.currentTimeMillis());
                entityMap.put(toString(fighterId), entity);
                // 标记该id已经用作傀儡
                campInfo.dummySet.add(fighterId);
            } else {
                break;
            }
        }
        campInfo.idleSet.addAll(idleSetList);
        return entityMap;
    }

    private boolean isOnline(long roleId) {
        if (familyWar instanceof FamilyWarKnockout) {
            Summary summary = ServiceHelper.summaryService().getSummary(roleId);
            return summary.isOnline();
        }
        return FamilyWarOnlinePlayerMap.isRoleOnline(roleId);
    }

    private boolean checkFighterState(FamilyBattleInfo campInfo, long fighterId) {
        FighterState fighterState = campInfo.fighterStateMap.get(fighterId);
        if (fighterState == null)
            return true;
        if (fighterState.state == BattleFighterState.FIGHTING || fighterState.state == BattleFighterState.AI_FIGHTING) {
            if ((System.currentTimeMillis() - fighterState.fightTimestamp) / 1000 > FamilyActWarManager.timeLimitOfNormalFight) {
                fighterState.state = BattleFighterState.IDLE;
                campInfo.idleSet.add(fighterId);
            }
        }
        if (fighterState.state == BattleFighterState.IDLE) {
            LogUtil.info("家族:{},成员状态空闲:{}", campInfo.familyId, fighterState.state);
            return true;
        }
        LogUtil.info("家族:{},成员状态战斗中:{}", campInfo.familyId, fighterState.state);
        return false;
    }

    public void restoreFighterState(long familyId, Set<String> fighterIds) {
        for (String fighterId : fighterIds) {
            updateFighterState(familyInfoMap.get(familyId), Long.parseLong(fighterId), BattleFighterState.IDLE, 0L);
        }
    }

    private void updateFighterState(FamilyBattleInfo campInfo, long fighterId, int state, long fightTimeStamp) {
        FighterState fighterState = campInfo.fighterStateMap.get(fighterId);
        if (fighterState == null) {
            fighterState = new FighterState();
            fighterState.figherId = fighterId;
            campInfo.fighterStateMap.put(fighterId, fighterState);
        }
        fighterState.fightTimestamp = fightTimeStamp;
        fighterState.state = state;
        if (fighterState.state == BattleFighterState.IDLE) {
            campInfo.idleSet.add(fighterId);
        }
    }

    public void checkEliteFightTimeout() {
//        LogUtil.info("familywar|camp1FamilyId:{},camp2FamilyId:{}|第{}场|已进行时间:{},本场战斗时间:{}|已结束时间:{},两场战斗间隔时间:{}",
//                camp1FamilyId, camp2FamilyId, screenings,
//                startFightTimeStamp == 0 ? "尚未开始" : (System.currentTimeMillis() - startFightTimeStamp) / 1000,
//                FamilyActWarManager.familywar_lasttime,
//                lastEndFightTimeStamp == 0 ? "尚未结束" : (System.currentTimeMillis() - lastEndFightTimeStamp) / 1000,
//                FamilyActWarManager.familywar_intervaltime);
        if (startFightTimeStamp != 0 && System.currentTimeMillis() - startFightTimeStamp > FamilyActWarManager.familywar_lasttime * 1000) {
            end(true);
        }
        if (lastEndFightTimeStamp != 0 && System.currentTimeMillis() - lastEndFightTimeStamp > FamilyActWarManager.familywar_intervaltime * 1000) {
            start(battleType);
        }
    }

    public void checkAndHandleTimeoutNormalFight() {
        List<String> normalFights = new ArrayList<>();
//        List<String> stageFightList = new ArrayList<>();
        for (Entry<String, FamilyWarNormalFight> entry : normalFightMap.entrySet()) {
            FamilyWarNormalFight normalFight = entry.getValue();
            normalFight.repairRevive();
            if (normalFight.checkTimeout()) {
                normalFight.end();
                normalFights.add(entry.getKey());
            }
        }
        for (String fight : normalFights) {
            normalFightMap.remove(fight);
        }
//        for (String fight : stageFightList) {
//            stageFightMap.remove(fight);
//        }
        if (eliteFight != null) {
            eliteFight.repairRevive();
        }
    }

    public void removeNormalFight(String fightId) {
        normalFightMap.remove(fightId);
    }

    public void removeStageFight(String fightId) {
        stageFightMap.remove(fightId);
    }

    public byte getFighterReviveState(String fighterUid, String fightId) {
        if (fightId.equals(eliteFightId) && eliteFight != null && !isEliteFinish) {
            return eliteFight.getFighterReviveState(fighterUid);
        } else {
            FamilyWarNormalFight normalFight = normalFightMap.get(fightId);
            if (normalFight == null) return 0;
            return normalFight.getFighterReviveState(fighterUid);
        }
    }

    public final int getMainServerId(long roleId) {
        long familyId = roleId2FamilyIdMap.get(roleId);
        if (familyId == camp1FamilyId) {
            return camp1MainServerId;
        } else if (familyId == camp2FamilyId) {
            return camp2MainServerId;
        }
        return 0;
    }

    private final List<Long> pollFromQueue(TreeMap<Long, Long> queue, int n) {
        List<Long> list = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            list.add(queue.pollFirstEntry().getKey());
        }
        return list;
    }

    public final void removeFighterFromBattle(long familyId, long roleId) {
        fightingInBattleRoleIdMap.remove(roleId);
        FamilyBattleInfo familyInfo = familyInfoMap.get(familyId);
        if (familyInfo != null) {
            updateFighterState(familyInfo, roleId, BattleFighterState.IDLE, 0L);
            familyInfo.dummySet.remove(roleId);
            if (familyInfo.waitingForDummySet.remove(roleId)) {
                familyInfo.waitingQueue.put(roleId, System.currentTimeMillis());
            } else {
                familyInfo.idleSet.add(roleId);
            }
        }
    }

    private long now() {
        return System.currentTimeMillis();
    }

    private String toString(long roleId) {
        return Long.toString(roleId);
    }

    public FamilyBattleInfo createBattleCampInfo(int campId, KnockoutFamilyInfo familyInfo) {
        FamilyBattleInfo campInfo = new FamilyBattleInfo();
        campInfo.familyId = familyInfo.getFamilyId();
        campInfo.familyName = familyInfo.getFamilyName();
        campInfo.campId = campId;
        campInfo.eliteFighterMap = new HashMap<>();
        campInfo.normalFighterMap = new HashMap<>();
        List<String> componentName = new ArrayList<>();
        componentName.add(MConst.Role);
        componentName.add(MConst.Skill);
        componentName.add(MConst.Deity);
        componentName.add(MConst.Buddy);
        for (KnockoutFamilyMemberInfo memberInfo : familyInfo.getMemberMap().values()) {
//            Summary summary = ServiceHelper.summaryService().getSummary(memberInfo.getMemberId());
//            if (isDummy(summary, componentName, memberInfo.getMemberId(), familyInfo)) continue;
//            ForeShowSummaryComponent fsSummary = (ForeShowSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(summary.getRoleId(), MConst.ForeShow);
//            if (!fsSummary.isOpen(ForeShowConst.FAMILYFIGHT)) continue;
            if (familyInfo.getTeamSheet().contains(memberInfo.getMemberId())) {
                campInfo.eliteFighterMap.put(memberInfo.getMemberId(), memberInfo.getFighterEntity());
            } else {
                campInfo.normalFighterMap.put(memberInfo.getMemberId(), memberInfo.getFighterEntity());
            }
            this.roleId2FamilyIdMap.put(memberInfo.getMemberId(), memberInfo.getFamilyId());
        }
        campInfo.totalFightScore = familyInfo.getTotalFightScore();
        campInfo.waitingQueue = new TreeMap<>(new Comparator<Long>() {
            @Override
            public int compare(Long l1, Long l2) {
                return l1 > l2 ? -1 : l1 < l2 ? 1 : 0;
            }
        });
        campInfo.idleSet = new HashSet<>(campInfo.normalFighterMap.keySet());
        campInfo.dummySet = new HashSet<>();
        campInfo.waitingForDummySet = new HashSet<>();
        campInfo.fighterStateMap = new HashMap<>();
        LogUtil.info("familywar|familyId:{},familyName:{},eliteFighter:{},normalFighter:{},idleSet:{}", campInfo.familyId, campInfo.familyName, campInfo.eliteFighterMap.keySet(), campInfo.normalFighterMap.keySet(), campInfo.idleSet);
        return campInfo;
    }

    private boolean isDummy(Summary summary, List<String> componentName, long memberId, KnockoutFamilyInfo familyInfo) {
        for (Map.Entry<String, SummaryComponent> componentEntry : summary.getComponentMap().entrySet()) {
            if (componentName.contains(componentEntry.getKey())) {
                if (componentEntry.getValue().isDummy()) {
                    LogUtil.info("{}是傀儡,精英成员是否包含:{}", memberId, familyInfo.getTeamSheet().contains(memberId));
                    return true;
                }
            }
        }
        return false;
    }

    public long getCamp1FamilyId() {
        return camp1FamilyId;
    }

    public long getCamp2FamilyId() {
        return camp2FamilyId;
    }

    public FamilyWarEliteFight getEliteFight() {
        return eliteFight;
    }

    public String getEliteFightId() {
        return eliteFightId;
    }

    public Map<Long, FamilyBattleInfo> getFamilyInfoMap() {
        return familyInfoMap;
    }

    public Map<Integer, FamilyWarEliteFightStat> getStatEliteList() {
        return statEliteList;
    }

    public FamilyWarEliteFightStat getMaxStat() {
        Set<Integer> integers = this.statEliteList.keySet();
        return this.statEliteList.get(Collections.max(integers));
    }

    public int getCamp1MainServerId() {
        return camp1MainServerId;
    }

    public int getCamp2MainServerId() {
        return camp2MainServerId;
    }

    public int getNormalFightRemainTime(String fightId) {
        FamilyWarNormalFight normalFight = normalFightMap.get(fightId);
        if (normalFight == null) return 0;
        long remainderTime = normalFight.getCreationTimestamp() + FamilyActWarManager.timeLimitOfNormalFight * 1000 - System.currentTimeMillis();
        return (int) (remainderTime / 1000);
    }

    class FamilyBattleInfo {
        private long familyId; // 家族id
        private String familyName; // 家族名字
        private int campId; // 阵营id
        private Map<Long, FighterEntity> eliteFighterMap; // 精英战场的名单
        private Map<Long, FighterEntity> normalFighterMap; // 普通战场的名单
        private long totalFightScore;

        private TreeMap<Long, Long> waitingQueue; // 等待队列
        private Set<Long> idleSet;
        private Set<Long> dummySet;
        private Set<Long> waitingForDummySet; // 离线玩家数据用于小战场后，该玩家再上线时的等待列表（需要等待小战场完成后，再进入匹配队列）
        //private Map<Long, Integer> fighterStateMap; // 家族成员状态
        private Map<Long, FighterState> fighterStateMap; // 普通战场家族成员状态
        private int morale; // 士气

    }

    class FighterState {
        private long figherId;
        private int state;
        private long fightTimestamp;
    }

}
