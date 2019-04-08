package com.stars.multiserver.familywar.qualifying;

import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.modules.familyactivities.war.event.FamilyWarEnterSafeSceneEvent;
import com.stars.modules.familyactivities.war.event.FamilyWarFightingOrNotEvent;
import com.stars.modules.familyactivities.war.packet.ClientFamilyWarBattleFightPersonalPoint;
import com.stars.modules.familyactivities.war.packet.ui.*;
import com.stars.modules.familyactivities.war.packet.ui.auxiliary.PkAuxQualifyFamilyWarPointsObj;
import com.stars.modules.familyactivities.war.packet.ui.auxiliary.PktAuxFamilyWarApplicant;
import com.stars.modules.familyactivities.war.packet.ui.auxiliary.PktAuxQualifyFamilyWarOpponent;
import com.stars.modules.familyactivities.war.prodata.FamilyWarRankAwardVo;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.serverLog.EventType;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.*;
import com.stars.multiserver.familywar.event.FamilyWarSupportEvent;
import com.stars.multiserver.familywar.flow.FamilyWarQualifyingFlow;
import com.stars.multiserver.familywar.knockout.FamilyWarKnockoutBattle;
import com.stars.multiserver.familywar.knockout.KnockoutFamilyInfo;
import com.stars.multiserver.familywar.knockout.KnockoutFamilyMemberInfo;
import com.stars.multiserver.familywar.knockout.fight.elite.FamilyWarEliteFightStat;
import com.stars.multiserver.familywar.qualifying.cache.FamilyWarQualifyingFixtureCache;
import com.stars.multiserver.familywar.remote.FamilyWarRemoteFamily;
import com.stars.multiserver.fight.FightIdCreator;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.services.activities.ActConst;
import com.stars.services.chat.ChatManager;
import com.stars.services.family.FamilyPost;
import com.stars.services.fightbase.FightBaseService;
import com.stars.services.role.RoleService;
import com.stars.util.LogUtil;
import com.stars.util.TimeUtil;
import com.stars.util.ranklist.IndexList;
import com.stars.util.ranklist.RankObj;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.stars.modules.familyactivities.war.FamilyActWarManager.*;
import static com.stars.multiserver.familywar.FamilyWarConst.*;

public class FamilyWarQualifying extends FamilyWar {


    /* 战斗表 */
    private Map<String, FamilyWarKnockoutBattle> battleMap; // 战斗表
    private Map<String, FamilyWarKnockoutBattle> _1STBattleMap; // 第一天积分赛
    private Map<String, FamilyWarKnockoutBattle> _2NDBattleMap; // 第二天积分赛
    private Map<String, FamilyWarKnockoutBattle> _3RDBattleMap; // 第三天积分赛
    private Map<String, FamilyWarKnockoutBattle> _4THBattleMap; // 第四天积分赛
    private Map<String, FamilyWarKnockoutBattle> _5ThBattleMap; // 第五天积分赛
    private Map<Integer, Map<String, FamilyWarKnockoutBattle>> battleTypeOfBattleMap;
    private int battleTpye;

    /**
     * Actor也有的数据，拷贝到qualifying,方便内存调用
     */
    private Map<Integer, List<Long>> groupIdfamilyIdsMap;//groupId,list of familyId
    private Map<Long, Integer> familyIdToGroupId;//familyId--groupId
    private Map<Integer, Map<Integer, List<FamilyWarQualifyingFixtureCache>>> fixtureCacheMap;//battleType , groupId, list of fixtrueCache
    private Map<Long, List<FamilyWarQualifyingFixtureCache>> familyIdCacheList;//familyId , list of cache
    private Map<Integer, IndexList> familyWinPointsMap;//groupId,indexList

    protected Map<Integer, IndexList> elitePointsRankListByGroup; // 精英战个人积分榜（前几名）
    protected Map<Integer, IndexList> normalPointsRankListByGroup; // 匹配战个人积分榜（前几名）

    public FamilyWarQualifying() {
        familyMap = new HashMap<>();
        memberMap = new HashMap<>();
        hasNoticeMasterMap = new HashMap<>();
        failFamilySet = new HashSet<>();
        battleMap = new HashMap<>();
        _1STBattleMap = new HashMap<>();
        _2NDBattleMap = new HashMap<>();
        _3RDBattleMap = new HashMap<>();
        _4THBattleMap = new HashMap<>();
        _5ThBattleMap = new HashMap<>();
        familyWinPointsMap = new HashMap<>();
        generateBattleTypeBattleMap();
    }

    private void generateBattleTypeBattleMap() {
        battleTypeOfBattleMap = new HashMap<>();
        battleTypeOfBattleMap.put(FamilyWarConst.Q_BATTLE_TYPE_1ST, _1STBattleMap);
        battleTypeOfBattleMap.put(FamilyWarConst.Q_BATTLE_TYPE_2ND, _2NDBattleMap);
        battleTypeOfBattleMap.put(FamilyWarConst.Q_BATTLE_TYPE_3RD, _3RDBattleMap);
        battleTypeOfBattleMap.put(FamilyWarConst.Q_BATTLE_TYPE_4TH, _4THBattleMap);
        battleTypeOfBattleMap.put(FamilyWarConst.Q_BATTLE_TYPE_5Th, _5ThBattleMap);
    }

    public void newGroupIdFamilyIdsMap(Map<Integer, List<Long>> map) {
        this.groupIdfamilyIdsMap = new HashMap<>(map);
        initOrResetPersonalPoint();
    }

    private void initOrResetPersonalPoint() {
        elitePointsRankListByGroup = new HashMap<>();
        normalPointsRankListByGroup = new HashMap<>();
        for (Integer groupId : groupIdfamilyIdsMap.keySet()) {
            elitePointsRankListByGroup.put(groupId, new IndexList(5000, 100, -5000));
            normalPointsRankListByGroup.put(groupId, new IndexList(5000, 100, -5000));
        }
    }

    public void newfamilyIdToGroupId(Map<Long, Integer> map) {
        this.familyIdToGroupId = new HashMap<>(map);
    }

    public void newfixtureCacheMap(Map<Integer, Map<Integer, List<FamilyWarQualifyingFixtureCache>>> map) {
        this.fixtureCacheMap = new HashMap<>(map);
    }

    public void newfamilyIdCacheList(Map<Long, List<FamilyWarQualifyingFixtureCache>> map) {
        this.familyIdCacheList = new HashMap<>(map);
    }

    public void sendMainIconToMaster(KnockoutFamilyInfo familyInfo) {
        LogUtil.info("familywar|给族长发设置名单的icon,失败的family:{},通知与否的集合:{}", failFamilySet, hasNoticeMasterMap);
        sendMainIcon(familyInfo.getMainServerId(), familyInfo.getMasterId(), familyInfo.getFamilyId(), FamilyWarConst.STATE_NOTICE_MASTER, 0L, FamilyWarConst.W_TYPE_QUALIFYING);
    }

    public void startBattle(int battleType) {
        LogUtil.info("familywar|{}战斗开始", battleType);
        this.battleTpye = battleType;
        Map<Integer, List<FamilyWarQualifyingFixtureCache>> cacheMap = fixtureCacheMap.get(battleType);
        for (List<FamilyWarQualifyingFixtureCache> cacheList : cacheMap.values()) {
            for (FamilyWarQualifyingFixtureCache cache : cacheList) {
                // FIXME: 2017-06-08 轮空处理
                if (cache.getCamp1FamilyId() == 0L && cache.getCamp2FamilyId() == 0L) {
                    LogUtil.info("familywar|轮空处理 , 双方都轮空 , camp1:{}, camp2:{}", cache.getCamp1FamilyId(), cache.getCamp2FamilyId());
                    continue;
                }
                if (cache.getCamp1FamilyId() == 0L) {
                    handleEmptyBattle(cache.getCamp2FamilyId(), cache.getBattleType());
                    LogUtil.info("familywar|轮空处理完毕,由于在{} 战斗中 {} 家族轮空，所以 {} 家族直接胜利 ", cache.getBattleType(), cache.getCamp1FamilyId(), cache.getCamp2FamilyId());
                    continue;
                }
                if (cache.getCamp2FamilyId() == 0L) {
                    handleEmptyBattle(cache.getCamp1FamilyId(), cache.getBattleType());
                    LogUtil.info("familywar|轮空处理完毕,由于在{} 战斗中 {} 家族轮空，所以 {} 家族直接胜利 ", cache.getBattleType(), cache.getCamp2FamilyId(), cache.getCamp1FamilyId());
                    continue;
                }
                String battleId = "fwq-" + cache.getBattleType() + "-" + cache.getGroupId() + "-" + "-" +
                        cache.getCamp1FamilyId() + "-" + cache.getCamp2FamilyId() + FightIdCreator.creatUUId();
                FamilyWarKnockoutBattle battle = new FamilyWarKnockoutBattle(battleId, battleType,
                        familyMap.get(cache.getCamp1FamilyId()), familyMap.get(cache.getCamp2FamilyId()));
                battle.setFamilyWar(this);
                battleMap.put(battleId, battle);
                putToBrachBattleMap(battleType, battleId, battle);
                familyMap.get(cache.getCamp1FamilyId()).setBattleId(battleId);
                familyMap.get(cache.getCamp2FamilyId()).setBattleId(battleId);
            }
        }
        for (FamilyWarKnockoutBattle battle : battleTypeOfBattleMap.get(battleType).values()) {
            battle.start(FamilyWarConst.W_TYPE_QUALIFYING);
        }
        startMatch();
    }

    private void handleEmptyBattle(long familyId, int battleType) {
        int groupId = familyIdToGroupId.get(familyId);
        KnockoutFamilyInfo winnerFamilyInfo = familyMap.get(familyId);
        updateFamilyPointRankObj(winnerFamilyInfo.getFamilyId(), true);
        for (FamilyWarQualifyingFixtureCache cache : fixtureCacheMap.get(battleType).get(groupId)) {
            if (isCache(cache, familyId)) {
                cache.setWinnerFamilyId(familyId);
            }
        }
        ServiceHelper.familyWarQualifyingService().updateFixtureCache(FamilyWarUtil.getFamilyWarServerId(), battleType, groupId, familyId);
    }

    public void endBattle(int battleType) {
        LogUtil.info("familywar|{}战斗结束", battleType);
        endMatch();
        for (FamilyWarKnockoutBattle battle : battleTypeOfBattleMap.get(battleType).values()) {
            battle.finishAllFight();
        }
        for (FamilyWarKnockoutBattle battle : battleTypeOfBattleMap.get(battleType).values()) {
            battleMap.remove(battle.getBattleId());
        }
        List<Long> roleIds = new ArrayList<>();
        for (KnockoutFamilyInfo familyInfo : familyMap.values()) {
            roleIds.addAll(familyInfo.getMemberMap().keySet());
            if (failFamilySet.contains(familyInfo.getFamilyId()))
                continue;
            hasNoticeMasterMap.put(familyInfo.getMasterId(), false);
        }
        if (battleType == FamilyWarConst.Q_BATTLE_TYPE_5Th) {
            List<FamilyWarRemoteFamily> remoteFamilyList = new ArrayList<>();
            for (IndexList rankList : familyWinPointsMap.values()) {
                List<RankObj> objList = rankList.getTop(2);
                for (RankObj obj : objList) {
                    QualifyingVictoryRankObj rankObj = (QualifyingVictoryRankObj) obj;
                    remoteFamilyList.add(new FamilyWarRemoteFamily(Long.parseLong(rankObj.getKey()), rankObj.getServerId()));
                }
            }
            ServiceHelper.familyWarQualifyingService().generateRemoteQulifications(FamilyWarUtil.getFamilyWarServerId(),
                    new ArrayList<>(remoteFamilyList));
        }
        try {
            unLockRoleState(roleIds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unLockRoleState(List<Long> roleIds) {
        for (long roleId : roleIds) {
            roleService().notice(getMainServerId(roleId), roleId, new FamilyWarFightingOrNotEvent(false));
        }
    }

    /**
     * 开启普通赛匹配线程
     */
    public void startMatch() {
        if (FamilyActWarManager.matchScheduler == null) {
            FamilyActWarManager.matchScheduler = Executors.newSingleThreadScheduledExecutor();
            FamilyActWarManager.matchScheduler.scheduleAtFixedRate(FamilyActWarManager.matchTaskQualifying,
                    FamilyActWarManager.normalMatchInterval, FamilyActWarManager.normalMatchInterval, TimeUnit.SECONDS);
        }
    }

    /**
     * 结束普通赛匹配线程
     */
    public void endMatch() {
        if (FamilyActWarManager.matchScheduler != null && !FamilyActWarManager.matchScheduler.isShutdown()) {
            FamilyActWarManager.matchScheduler.shutdown();
            FamilyActWarManager.matchScheduler = null;
        }
    }

    private void putToBrachBattleMap(int battleType, String battleId, FamilyWarKnockoutBattle battle) {
        switch (battleType) {
            case FamilyWarConst.Q_BATTLE_TYPE_1ST:
                _1STBattleMap.put(battleId, battle);
                break;
            case FamilyWarConst.Q_BATTLE_TYPE_2ND:
                _2NDBattleMap.put(battleId, battle);
                break;
            case FamilyWarConst.Q_BATTLE_TYPE_3RD:
                _3RDBattleMap.put(battleId, battle);
                break;
            case FamilyWarConst.Q_BATTLE_TYPE_4TH:
                _4THBattleMap.put(battleId, battle);
                break;
            case FamilyWarConst.Q_BATTLE_TYPE_5Th:
                _5ThBattleMap.put(battleId, battle);
        }
    }

    public void match() {
        for (FamilyWarKnockoutBattle battle : battleTypeOfBattleMap.get(battleTpye).values()) {
            try {
                battle.match(FamilyWarConst.W_TYPE_QUALIFYING);
            } catch (Exception e) {
                LogUtil.info("familywar|匹配出现异常 battleId:{}", battle.getBattleId());
                e.printStackTrace();
            }
        }
    }

    /**
     * 是否参赛家族
     *
     * @param familyId
     * @return
     */
    public boolean isQualifyingFamily(long familyId) {
        return familyMap.containsKey(familyId) && !failFamilySet.contains(familyId);
    }

    /**
     * fixme : 结果入库
     *
     * @param battleId
     * @param winnerFamilyId
     * @param loserFamilyId
     */
    @Override
    public void finishBattle(String battleId, long winnerFamilyId, long loserFamilyId) {
        FamilyWarKnockoutBattle battle = battleMap.remove(battleId);
        if (battle == null) {
            LogUtil.error("familywar|不存在对战, battleId=" + battleId);
            return;
        }
        int battleType = battle.getType();
        int groupId = familyIdToGroupId.get(winnerFamilyId);
        KnockoutFamilyInfo winnerFamilyInfo = familyMap.get(winnerFamilyId);
        KnockoutFamilyInfo loserFamilyInfo = familyMap.get(loserFamilyId);
        winnerFamilyInfo.setWinner(true);
        loserFamilyInfo.setWinner(true);
        updateFamilyPointRankObj(winnerFamilyInfo.getFamilyId(), true);
        updateFamilyPointRankObj(loserFamilyInfo.getFamilyId(), false);
        for (FamilyWarQualifyingFixtureCache cache : fixtureCacheMap.get(battleType).get(groupId)) {
            if (isCache(cache, winnerFamilyId)) {
                cache.setWinnerFamilyId(winnerFamilyId);
            }
        }
        ServiceHelper.familyWarQualifyingService().updateFixtureCache(FamilyWarUtil.getFamilyWarServerId(), battleType, groupId, winnerFamilyId);
    }

    private boolean isCache(FamilyWarQualifyingFixtureCache cache, long winnerFamilyId) {
        return cache.getCamp1FamilyId() == winnerFamilyId || cache.getCamp2FamilyId() == winnerFamilyId;
    }

    public void updateFamilyPointRankObj(long familyId, boolean winOrLose) {
        int groupId = familyIdToGroupId.get(familyId);
        IndexList rankList = familyWinPointsMap.get(groupId);
        if (rankList == null) {
            LogUtil.info("familywar| {} 没有对应的家族积分排行榜", groupId);
            familyWinPointsMap.put(groupId, rankList = new IndexList(100, 10, -100));
        }
        if (rankList.containsRank(Long.toString(familyId))) {
            QualifyingVictoryRankObj rankObj = (QualifyingVictoryRankObj) rankList.getRankObjByKey(Long.toString(familyId));
            if (winOrLose) {
                long point = rankObj.getPoints();
                rankObj.addVictory();
                rankList.updateRank(Long.toString(familyId), FamilyWarConst.winSorce + point);
            } else {
                rankObj.addDefeat();
            }
        } else {
            if (winOrLose) {
                rankList.addRank(Long.toString(familyId), new QualifyingVictoryRankObj(Long.toString(familyId), FamilyWarConst.winSorce, familyMap.get(familyId).getMainServerId(),
                        familyMap.get(familyId).getFamilyName(), 1, 0, familyMap.get(familyId).getTotalFightScore()));
            } else {
                rankList.addRank(Long.toString(familyId), new QualifyingVictoryRankObj(Long.toString(familyId), 0L, familyMap.get(familyId).getMainServerId(),
                        familyMap.get(familyId).getFamilyName(), 0, 1, familyMap.get(familyId).getTotalFightScore()));
            }

        }

    }

    @Override
    public FightBaseService fightService() {
        return FamilyWarRpcHelper.fightBaseService();
    }

    @Override
    public RoleService roleService() {
        return FamilyWarRpcHelper.roleService();
    }

    @Override
    public void removeBattle() {
        battleMap.clear();
    }

    @Override
    public void removeBattle(String battleId, int battleType) {
        battleTypeOfBattleMap.get(battleType).remove(battleId);
    }

    @Override
    public Map<Long, KnockoutFamilyInfo> getFamilyMap() {
        return familyMap;
    }

    @Override
    public void updateElitePoints(String fighterUid, long delta) {
        updatePoints(elitePointsMap, elitePointsRankList, fighterUid, delta);
        updatePointsByGroup(elitePointsMap, elitePointsRankListByGroup, fighterUid, delta);
    }

    @Override
    public void updateNormalPoints(String fighterUid, long delta) {
        updatePoints(normalPointsMap, normalPointsRankList, fighterUid, delta);
        updatePointsByGroup(normalPointsMap, normalPointsRankListByGroup, fighterUid, delta);
    }

    private void updatePointsByGroup(Map<String, Long> pointsMap, Map<Integer, IndexList> rankListMap, String fighterUid, long delta) {
        int groupId = familyIdToGroupId.get(memberMap.get(Long.parseLong(fighterUid)).getFamilyId());
        IndexList rankList = rankListMap.get(groupId);
        long points = pointsMap.get(fighterUid);
        if (rankList.containsRank(fighterUid)) {
            rankList.updateRank(fighterUid, points);
        } else {
            KnockoutFamilyMemberInfo memberInfo = memberMap.get(Long.parseLong(fighterUid));
            if (memberInfo == null) return;
            KnockoutFamilyInfo familyInfo = familyMap.get(memberInfo.getFamilyId());
            if (familyInfo == null) return;
            rankList.addRank(fighterUid, new FamilyWarPointsRankObj(
                    fighterUid, points, memberInfo.getMainServerId(), memberInfo.getName(), familyInfo.getFamilyName()));
        }
        LogUtil.info("familywar|增加的积分 roleId:{},rank:{},point:{},delta:{}", fighterUid, rankList.getRank(fighterUid), points, delta);
    }

    /**
     * 发送积分排行榜信息
     *
     * @param mainServerId
     * @param roleId
     * @param subtype
     */
    public void sendPointsRank(int mainServerId, long roleId, byte subtype, byte warType) {
        IndexList rankList = null;
        if (memberMap.get(roleId) == null) return;
        switch (subtype) {
            case ServerFamilyWarUiPointsRank.SUBTYPE_ELITE_FIGHT:
                rankList = elitePointsRankListByGroup.get(familyIdToGroupId.get(memberMap.get(roleId).getFamilyId()));
                break;
            case ServerFamilyWarUiPointsRank.SUBTYPE_NORMAL_FIGHT:
                rankList = normalPointsRankListByGroup.get(familyIdToGroupId.get(memberMap.get(roleId).getFamilyId()));
                break;
            default:
                return;
        }
        ClientFamilyWarUiPointsRank packet = new ClientFamilyWarUiPointsRank(
                subtype, warType, getPktAuxFamilyWarPointsObjList(rankList, 100));
        RankObj myRankObj = rankList.getRankObjByKey(Long.toString(roleId));
        packet.setMyRank(rankList.getRank(Long.toString(roleId)));
        packet.setMyRankObj(createPointsObj(myRankObj));
        roleService().send(mainServerId, roleId, packet);
    }

    @Override
    public void enterSafeScene(int controlServerId, int mainServerId, long roleId) {
        KnockoutFamilyMemberInfo memberInfo = memberMap.get(roleId);
        if (memberInfo != null) {
            enterSafeScene(controlServerId, mainServerId, memberInfo.getFamilyId(), roleId);
        }
    }

    @Override
    public void enterSafeScene(int controlServerId, int mainServerId, long familyId, long roleId) {
        LogUtil.info("familywar|家族:{} 的玩家:{} 请求进入备战场景", familyId, roleId);
        KnockoutFamilyInfo familyInfo = familyMap.get(familyId);
        if (familyInfo == null) {
            PacketManager.send(roleId, new ClientText("非参战家族不能进入备战场景"));
            return;
        }
        FamilyWarKnockoutBattle battle = battleMap.get(familyInfo.getBattleId());
        boolean isFinish = false;
        if (battle != null) {
            isFinish = battle.isEliteFinish();
        }
        byte memberType = 0;
        byte initType = 0;
        long myFamilyTotalPoints = 0;
        long enemyFamilyTotalPoints = 0;
        LogUtil.info("familywar|isEliteFinish:{}", isFinish);
        if (familyInfo.getTeamSheet().contains(roleId)) {
            initType = 2;
        } else if (familyInfo.getMemberMap().containsKey(roleId)) {
            initType = 1;
        } else {
            initType = 0;
        }
        if (familyInfo.getTeamSheet().contains(roleId) && !isFinish) {
            memberType = 2;
        } else if (familyInfo.getMemberMap().containsKey(roleId) || isFinish) {
            memberType = 1;
        } else {
            memberType = 0;
        }
        if (battle != null && battle.getStatEliteList() != null) {
            for (FamilyWarEliteFightStat stat : battle.getStatEliteList().values()) {
                if (battle.getCamp1FamilyId() == familyId) {
                    myFamilyTotalPoints += stat.getCamp1TotalPoints();
                    enemyFamilyTotalPoints += stat.getCamp2TotalPoints();
                } else {
                    myFamilyTotalPoints += stat.getCamp2TotalPoints();
                    enemyFamilyTotalPoints += stat.getCamp1TotalPoints();
                }
            }
        }
        if (myFamilyTotalPoints == 0L) {
            myFamilyTotalPoints = FamilyActWarManager.originalPoints;
        }
        if (enemyFamilyTotalPoints == 0L) {
            enemyFamilyTotalPoints = FamilyActWarManager.originalPoints;
        }
        FamilyWarRpcHelper.roleService().notice(mainServerId, roleId,
                new FamilyWarEnterSafeSceneEvent(initType, memberType, myFamilyTotalPoints, enemyFamilyTotalPoints,
                        FamilyWarUtil.getNearBattleEndTimeL(ActConst.ID_FAMILY_WAR_QUALIFYING) - System.currentTimeMillis()));
    }

    @Override
    public void enter(int controlServerId, int mainServerId, long familyId, long roleId, FighterEntity fighterEntity) {
        int thisStep = FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW;
        LogUtil.info("familywar|家族:{} 的玩家:{} 请求进入战场|当前步数:{}|获得最近一场比赛的开始时间:{}", familyId, roleId, thisStep,
                FamilyWarUtil.getNearBattleTimeStr(ActConst.ID_FAMILY_WAR_QUALIFYING));
        if (thisStep == FamilyWarQualifyingFlow.STEP_BEFORE_1ST ||
                thisStep == FamilyWarQualifyingFlow.STEP_BEFORE_2ND ||
                thisStep == FamilyWarQualifyingFlow.STEP_BEFORE_3RD ||
                thisStep == FamilyWarQualifyingFlow.STEP_BEFORE_4TH ||
                thisStep == FamilyWarQualifyingFlow.STEP_BEFORE_5TH) {
            long remainTime = (FamilyWarUtil.getNearBattleTimeL(ActConst.ID_FAMILY_WAR_QUALIFYING) - System.currentTimeMillis()) / 1000;
            if (remainTime <= 0) {
                remainTime = 1;
            }
            roleService().warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_nextround"), remainTime));
            return;
        }
        StringBuilder stringBuilder = TimeUtil.getChinaShow(FamilyWarUtil.getNearBattleTimeL(ActConst.ID_FAMILY_WAR_QUALIFYING));
        if (thisStep == FamilyWarQualifyingFlow.STEP_END_1ST ||
                thisStep == FamilyWarQualifyingFlow.STEP_END_2ND ||
                thisStep == FamilyWarQualifyingFlow.STEP_END_3RD ||
                thisStep == FamilyWarQualifyingFlow.STEP_END_4TH ||
                thisStep == FamilyWarQualifyingFlow.STEP_END_5TH ||
                thisStep == FamilyWarQualifyingFlow.STEP_END_QUALIFYING) {
            roleService().warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_nexturn"), stringBuilder.toString()));
            return;
        }
        KnockoutFamilyInfo familyInfo = familyMap.get(familyId);
        if (familyInfo == null) {
            PacketManager.send(roleId, new ClientText("非参战家族不能进入战场"));
            return;
        }
        FamilyWarKnockoutBattle battle = battleMap.get(familyInfo.getBattleId());
        boolean isEliteFinish = false;
        boolean isFighting = false;
        if (battle != null) {
            isEliteFinish = battle.isEliteFinish();
        }
        LogUtil.info("familywar|精英战场是否结束:{}", isEliteFinish);
        if (familyInfo.getTeamSheet().contains(roleId) && !isEliteFinish) {
            enterEliteFight(controlServerId, mainServerId, familyId, roleId, fighterEntity);

        } else if (familyInfo.getMemberMap().containsKey(roleId) || isEliteFinish) {
            enterNormalFightWaitingQueue(controlServerId, mainServerId, familyId, roleId, fighterEntity);
        }
    }

    @Override
    public void enterEliteFight(int controlServerId, int mainServerId, long familyId, long roleId, FighterEntity fighterEntity) {
        KnockoutFamilyInfo familyInfo = familyMap.get(familyId);
        if (familyInfo == null) {
            roleService().warn(mainServerId, roleId, "no family");
            return;
        }
        LogUtil.info("familywar|进入精英战场,roleId:{},familyMember:{}", roleId, familyInfo.getMemberMap().keySet());
        if (!familyInfo.getTeamSheet().contains(roleId)) {
            roleService().warn(mainServerId, roleId, "not in elite fight team");
            return;
        }
        String battleId = familyInfo.getBattleId();
        if (battleId == null || "".equals(battleId)) {
            roleService().warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_begintext"), FamilyWarUtil.getNearBattleTimeStr(ActConst.ID_FAMILY_WAR_QUALIFYING)));
            return;
        }
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle != null) {
            if (battle.isEliteFinish()) {
                roleService().warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_desc_eliteover")));
                return;
            }
            if (!battle.isFighting()) {
                long remainTime = ((battle.getLastEndFightTimeStamp() + FamilyActWarManager.familywar_intervaltime * 1000) - System.currentTimeMillis()) / 1000;
                if (remainTime <= 0) {
                    remainTime = 1;
                }
                roleService().warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_nextround"), remainTime));
                //// FIXME: 2017-04-17 这里的下一场时间
                return;
            }
            familyInfo.getMemberMap().get(roleId).setFighterEntity(fighterEntity);
            battle.enterEliteFight(mainServerId, familyId, roleId, fighterEntity);
        } else {
            StringBuilder stringBuilder = TimeUtil.getChinaShow(FamilyWarUtil.getNearBattleTimeL(ActConst.ID_FAMILY_WAR_QUALIFYING));
            roleService().warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_nexturn"), stringBuilder.toString()));
        }
    }

    @Override
    public void enterNormalFightWaitingQueue(int controlServerId, int mainServerId, long familyId, long roleId, FighterEntity fighterEntity) {
// 根据familyId找到对应的battleId，如果familyId/battleId不存在，则提示
        KnockoutFamilyInfo familyInfo = familyMap.get(familyId);
        if (familyInfo == null) {
            roleService().warn(mainServerId, roleId, "no family");
            return;
        }
        LogUtil.info("familywar|进入匹配队列,roleId{},familyMember:{}", roleId, familyInfo.getMemberMap().keySet());
        if (!familyInfo.getMemberMap().containsKey(roleId)) {
            roleService().warn(mainServerId, roleId, "not in normal fight team1");
            return;
        }
        String battleId = familyInfo.getBattleId();
        if (battleId == null || "".equals(battleId)) {
            roleService().warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_begintext"), FamilyWarUtil.getNearBattleTimeStr(ActConst.ID_FAMILY_WAR_QUALIFYING)));
            return;
        }
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle != null) {
            familyInfo.getMemberMap().get(roleId).setFighterEntity(fighterEntity);
            battle.enterNormalFightWaitingQueue(controlServerId, mainServerId, familyId, roleId, fighterEntity);
        } else {
            if (familyInfo.isWinner()) {
                roleService().warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_begintext"), FamilyWarUtil.getNearBattleTimeStr(ActConst.ID_FAMILY_WAR_QUALIFYING)));
            } else {
                roleService().warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_fightover")));
            }
        }
    }

    @Override
    public void cancelNormalFightWaitingQueue(int controlServerId, int mainServerId, long familyId, long roleId) {
        KnockoutFamilyInfo familyInfo = familyMap.get(familyId);
        if (familyInfo == null) {
            roleService().warn(mainServerId, roleId, "no family");
            return;
        }
        LogUtil.info("roleId{},familyMember:{}", roleId, familyInfo.getMemberMap().keySet());
        if (!familyInfo.getMemberMap().containsKey(roleId)) {
            roleService().warn(mainServerId, roleId, "not in normal fight team0");
            return;
        }
        String battleId = familyInfo.getBattleId();
        if (battleId == null || "".equals(battleId)) {
            roleService().warn(mainServerId, roleId, "no battle id");
            return;
        }
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle != null) {
            battle.cancelNormalFightWaitingQueue(controlServerId, mainServerId, familyId, roleId);
        } else {
            roleService().warn(mainServerId, roleId, "no battle");
        }
    }

    public void onEliteFighterAddingSucceeded(int mainServerId, int fightServerId, String battleId, String fightId, long roleId) {
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle == null) return;
        battle.handleFighterEnter(roleId, fightId);
    }

    @Override
    public void handleFighterQuit(String battleId, long roleId, String fightId) {
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle != null) {
            battle.handleFighterQuit(roleId, fightId);
        }
    }

    public final void onClientPreloadFinished(int mainServerId, String battleId, String fightId, long roleId) {
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle != null) {
            battle.onClientPreloadFinished(mainServerId, fightId, roleId);
        } else {
            LogUtil.info("familywar|onClientPreloadFinished: no such battle {}", battleId);
        }
    }

    @Override
    public IndexList getElitePointsRankList(long roleId) {
        long familyId = memberMap.get(roleId).getFamilyId();
        int groupId = familyIdToGroupId.get(familyId);
        return elitePointsRankListByGroup.get(groupId);
    }

    @Override
    public IndexList getNormalPointsRankList(long roleId) {
        long familyId = memberMap.get(roleId).getFamilyId();
        int groupId = familyIdToGroupId.get(familyId);
        return normalPointsRankListByGroup.get(groupId);
    }

    /**
     * 处理复活请求
     *
     * @param mainServerId
     * @param battleId
     * @param fightId
     * @param roleId
     * @param reqType
     */
    public void revive(int mainServerId, String battleId, String fightId, long roleId, byte reqType) {
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle != null) {
            battle.handleRevive(fightId, Long.toString(roleId), reqType);
        }
    }

    /**
     * 付费玩家复活
     *
     * @param battleId
     * @param fightId
     * @param fighterUid
     */
    public void handleRevive(String battleId, String fightId, String fighterUid) {
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle != null) {
            battle.handleRevive(fightId, fighterUid);
        }
    }

    /**
     * 获得关卡战斗中的FighterEntity
     *
     * @param battleId
     * @param fightId
     * @return
     */
    public Map<String, FighterEntity> getStageFighterEntities(String battleId, String fightId) {
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle == null) return null;
        return battle.getStageFighterEntities(fightId);
    }

    public void onNormalFightCreationSucceeded(int mainServerId, int fightServerId, String battleId, String fightId) {
        LogUtil.info("familywar|battleId={}, fightServerId={}, fightId={}", battleId, fightServerId, fightId);
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle == null) return;
        battle.onNormalFightCreationSucceeded(mainServerId, fightServerId, fightId);
    }

    public void onNormalFightStarted(int mainServerId, int fightServerId, String battleId, String fightId) {
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle == null) return;
        battle.onNormalFightStarted(mainServerId, fightServerId, fightId);
    }

    public void onStageFightCreateSucceeded(int mainServerId, int fightServerId, String battleId, String fightId, long roleId) {
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle == null) return;
        battle.handleFighterEnter(roleId, fightId);
        roleService().send(getMainServerId(roleId), roleId, new ClientFamilyWarBattleFightPersonalPoint(battle.getBattleNormalPersonalPosints(Long.toString(roleId))));
    }

    public void handleDamage(String battleId, String fightId, Map<String, HashMap<String, Integer>> damageMap) {
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle != null) {
            battle.handleDamage(fightId, damageMap);
        } else {
            LogUtil.info("familywar|handleEliteFightDead: no such battle {}", battleId);
        }
    }

    public void handleDead(String battleId, String fightId, Map<String, String> deadMap) {
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle != null) {
            battle.handleDead(fightId, deadMap);
        } else {
            LogUtil.info("familywar|handleEliteFightDead: no such battle {}", battleId);
        }
    }

    public final void syncBattleFightUpdateInfo() {
        for (FamilyWarKnockoutBattle battle : battleMap.values()) {
            try {
                battle.sendBattleFightUpdateInfo();
            } catch (Exception e) {
                LogUtil.error("", e);
            }
        }
    }

    /**
     * 时间检测
     */
    public void checkAndEndTimeout() {
        for (FamilyWarKnockoutBattle battle : battleMap.values()) {
            battle.checkAndHandleTimeoutNormalFight();//检测匹配战场结束时间
            battle.checkEliteFightTimeout();
        }
    }

    public void sendAward_ResetPoints_NoticeMater(int step, long countdown) {
        sendAward(step);
        resetPoint();
        noticeMater(step, countdown);
    }

    private void sendAward(int step) {
        LogUtil.info("familywar|发奖阶段|step:{}", step);
        sendRolePointsRank();
        sendFamilyRankAwardAndUnlock(step);
    }

    private void sendRolePointsRank() {
        // 个人积分发奖
        Map<Integer, Map<Long, Integer>> map = new HashMap<>(); // (serverId, (roleId, points))
        // 发精英的
        // 先分组
        for (IndexList rankList : elitePointsRankListByGroup.values()) {
            int rank = 1;
            for (RankObj obj : rankList.getAll()) {
                FamilyWarPointsRankObj rankObj = (FamilyWarPointsRankObj) obj;
                Map<Long, Integer> submap = map.get(rankObj.getServerId());
                if (submap == null) {
                    map.put(rankObj.getServerId(), submap = new HashMap<>());
                }
                submap.put(Long.parseLong(rankObj.getKey()), rank++);
            }
        }

        // 调用localservice进行发奖
        for (Map.Entry<Integer, Map<Long, Integer>> entry : map.entrySet()) {
            ServiceHelper.familyWarQualifyingService().sendPointsRankAward(FamilyWarUtil.getFamilyWarServerId(), entry.getKey(), true, entry.getValue());
        }

        // 发匹配的
        // 先分组
        map = new HashMap<>();
        for (IndexList rankList : normalPointsRankListByGroup.values()) {
            int rank = 1;
            for (RankObj obj : rankList.getAll()) {
                FamilyWarPointsRankObj rankObj = (FamilyWarPointsRankObj) obj;
                Map<Long, Integer> submap = map.get(rankObj.getServerId());
                if (submap == null) {
                    map.put(rankObj.getServerId(), submap = new HashMap<Long, Integer>());
                }
                submap.put(Long.parseLong(rankObj.getKey()), rank++);
            }
        }
        // 调用localservice进行发奖
        for (Map.Entry<Integer, Map<Long, Integer>> entry : map.entrySet()) {
            ServiceHelper.familyWarQualifyingService().sendPointsRankAward(FamilyWarUtil.getFamilyWarServerId(), entry.getKey(), false, entry.getValue());
        }
    }

    public void sendFamilyRankAwardAndUnlock(int step) {
        if (step == FamilyWarQualifyingFlow.STEP_END_QUALIFYING) {
            for (long familyId : familyMap.keySet()) {
                int groupId = familyIdToGroupId.get(familyId);
                IndexList rankList = familyWinPointsMap.get(groupId);
                if (rankList == null) continue;
                sendFamilyRankAward(W_TYPE_QUALIFYING, familyId, rankList.getRank(Long.toString(familyId)));
            }
            Map<Integer, List<Long>> serverFamilyMap = new HashMap<>();
            for (Map.Entry<Long, KnockoutFamilyInfo> entry : familyMap.entrySet()) {
                List<Long> familyIdList = serverFamilyMap.get(entry.getValue().getMainServerId());
                if (familyIdList == null) {
                    familyIdList = new ArrayList<>();
                    serverFamilyMap.put(entry.getValue().getMainServerId(), familyIdList);
                }
                familyIdList.add(entry.getKey());
            }
            for (Map.Entry<Integer, List<Long>> entry : serverFamilyMap.entrySet()) {
                FamilyWarRpcHelper.familyWarService().unLockFamily(entry.getKey(), entry.getValue());
            }
        } else {
            for (KnockoutFamilyInfo info : familyMap.values()) {
                FamilyWarRpcHelper.familyWarService().halfLockFamily(info.getMainServerId(), info.getFamilyId());
            }
        }
    }

    /**
     * 发送家族排名奖励
     *
     * @param period
     * @param familyId
     * @param rank
     */
    private void sendFamilyRankAward(int period, long familyId, int rank) {
        KnockoutFamilyInfo familyInfo = familyMap.get(familyId);
        if (familyInfo == null) {
            LogUtil.error("familywar|家族排名奖励，familyId=" + familyId);
            return;
        }
        Map<Long, Integer> rankAwardMap = new HashMap<>(); // roleId -> familyWarRankAwardVoId
        for (KnockoutFamilyMemberInfo memberInfo : familyInfo.getMemberMap().values()) {
            long memberId = memberInfo.getMemberId();
            byte postId = memberInfo.getPostId();
            FamilyWarRankAwardVo awardVo = null;
            if (postId == FamilyPost.MASTER_ID) {
                awardVo = getFamilyRankAwardVo(period, rank, RANK_AWARD_OBJ_TYPE_MASTER);
            } else if (familyInfo.getTeamSheet().contains(memberId)) {
                awardVo = getFamilyRankAwardVo(period, rank, RANK_AWARD_OBJ_TYPE_FIGHTER);
            } else {
                awardVo = getFamilyRankAwardVo(period, rank, RANK_AWARD_OBJ_TYPE_MEMBER);
            }
            if (awardVo != null) {
                rankAwardMap.put(memberId, awardVo.getId());
            }
        }
        try {
            ServiceHelper.familyWarQualifyingService().sendFamilyRankAward(FamilyWarUtil.getFamilyWarServerId(), familyInfo.getMainServerId(), familyInfo.getFamilyName(), rank, rankAwardMap);
        } catch (Exception e) {
            LogUtil.info("familywar|发送家族:{}排名奖励出现异常|{}", familyId, e);
        }
    }

    private void resetPoint() {
        elitePointsRankList = new IndexList(5000, 100, -5000);
        normalPointsRankList = new IndexList(5000, 100, -5000);
        elitePointsMap = new HashMap<>();
        normalPointsMap = new HashMap<>();
        eliteMinPointsAwardAcquiredRecordSet = new ConcurrentHashMap<>();
        normalMinPointsAwardAcquiredRecordSet = new ConcurrentHashMap<>();
        initOrResetPersonalPoint();
    }

    private void noticeMater(int step, long countdown) {
        LogUtil.info("通知族长");
        if (step != FamilyWarQualifyingFlow.STEP_END_QUALIFYING) {
            LogUtil.info("familywar|step:{}", step);
            sendMainIconToMaster(FamilyWarConst.W_TYPE_QUALIFYING);
        }
    }

    public void viewMinPointsAward(int mainServerId, long roleId) {
        byte minAwardType = MIN_AWARD_QUALIFYING_ELITE;
        Long points;
        Set<Long> recordSet = null;
        if (elitePointsMap.containsKey(Long.toString(roleId))) {
            minAwardType = MIN_AWARD_QUALIFYING_ELITE;
            points = elitePointsMap.get(Long.toString(roleId));
            recordSet = eliteMinPointsAwardAcquiredRecordSet.get(roleId);
        } else if (normalPointsMap.containsKey(Long.toString(roleId))) {
            minAwardType = MIN_AWARD_QUALIFYING_NORMAL;
            points = normalPointsMap.get(Long.toString(roleId));
            recordSet = normalMinPointsAwardAcquiredRecordSet.get(roleId);
        } else {
            points = null;
        }
        LogUtil.info("familywar|{} 查看积分:{}", roleId, points);
        ClientFamilyWarUiMinPointsAward packet = new ClientFamilyWarUiMinPointsAward(
                ClientFamilyWarUiMinPointsAward.SUBTYPE_VIEW, minAwardType, points == null ? 0 : points.longValue(), recordSet);
        roleService().send(mainServerId, roleId, packet);
    }


    public void acquireMinPointsAward(int mainServerId, long roleId, long acquirePoints) {
        if (!memberMap.containsKey(roleId)) { // 非家族成员则不发奖
            roleService().warn(mainServerId, roleId, "非家族成员则不发奖");
            return;
        }
        if (elitePointsMap.containsKey(Long.toString(roleId))) {
            Set<Long> recordSet = eliteMinPointsAwardAcquiredRecordSet.get(roleId);
            if (recordSet == null) {
                recordSet = new HashSet<>();
                eliteMinPointsAwardAcquiredRecordSet.put(roleId, recordSet);
            }
            acquireMinPointsAward0(mainServerId, roleId, acquirePoints,
                    elitePointsMap, recordSet, eliteMinPointsAwardMapForQualify, FamilyWarConst.MIN_AWARD_QUALIFYING_ELITE);
        } else if (normalPointsMap.containsKey(Long.toString(roleId))) {
            Set<Long> recordSet = normalMinPointsAwardAcquiredRecordSet.get(roleId);
            if (recordSet == null) {
                recordSet = new HashSet<>();
                normalMinPointsAwardAcquiredRecordSet.put(roleId, recordSet);
            }
            acquireMinPointsAward0(mainServerId, roleId, acquirePoints,
                    normalPointsMap, recordSet, normalMinPointsAwardMapForQualify, FamilyWarConst.MIN_AWARD_QUALIFYING_NORMAL);
        }
    }

    /**
     * 领取积分奖励
     *
     * @param mainServerId
     * @param roleId
     * @param acquirePoints
     * @param pointsMap
     * @param recordSet
     * @param awardMap
     * @param awardType
     */
    private void acquireMinPointsAward0(int mainServerId, long roleId, long acquirePoints,
                                        Map<String, Long> pointsMap, Set<Long> recordSet,
                                        Map<Long, Map<Integer, Integer>> awardMap, byte awardType) {
        long currentPoints = pointsMap.get(Long.toString(roleId));
        if (acquirePoints > currentPoints) {
            roleService().warn(mainServerId, roleId, "积分未足够");
            return;
        }
        if (recordSet.contains(acquirePoints)) {
            roleService().warn(mainServerId, roleId, "已领取");
            return;
        }
        if (!awardMap.containsKey(acquirePoints)) {
            roleService().warn(mainServerId, roleId, "不存在产品数据");
            return;
        }
        Map<Integer, Integer> toolMap = awardMap.get(acquirePoints);
        FamilyWarRpcHelper.familyWarService().sendAward(mainServerId, roleId, EventType.FAMILY_WAR_PERSONAL_POINT.getCode(), emailTemplateIdOfMinPointsAward, toolMap);
//        localService.sendAward(mainServerId, roleId, EventType.FAMILY_WAR_PERSONAL_POINT.getCode(), emailTemplateIdOfMinPointsAward, toolMap);
        recordSet.add(acquirePoints);
        ClientFamilyWarUiMinPointsAward packet = new ClientFamilyWarUiMinPointsAward(
                ClientFamilyWarUiMinPointsAward.SUBTYPE_ACQUIRE, awardType, acquirePoints, recordSet);
        roleService().send(mainServerId, roleId, packet);
    }

    /* 报名精英战/确定精英战名单 */
    public void sendApplicationSheet(int mainServerId, long familyId, long roleId) {
        // 判断阶段
        KnockoutFamilyInfo familyInfo = familyMap.get(familyId);
        if (familyInfo == null) {
            roleService().warn(mainServerId, roleId, "报名失败，您的家族不在参战家族名单里");
            return;
        }
        if (!familyInfo.getMemberMap().containsKey(roleId)) {
            roleService().warn(mainServerId, roleId, "报名失败");
            return;
        }
        ClientFamilyWarUiApply packet = new ClientFamilyWarUiApply();
        KnockoutFamilyMemberInfo selfInfo = familyInfo.getMemberMap().get(roleId);
        // 设置自身的资格
        if (selfInfo.getPostId() == FamilyPost.MASTER_ID) {
            packet.setSelfQualification(K_SELF_QUAL_MASTER);
        } else if (familyInfo.getApplicationSheet().contains(roleId)) {
            packet.setSelfQualification(K_SELF_QUAL_APPLIED);
        } else {
            packet.setSelfQualification(K_SELF_QUAL_NOT_APPLIED);
        }
        packet.setLock(Packet.FALSE);
        // 增加已申请人的列表
        Set<Long> applicationSheet = new HashSet<>(familyInfo.getApplicationSheet());
        Map<Long, PktAuxFamilyWarApplicant> applicants = new HashMap<>();
        for (long applicantId : applicationSheet) {
            KnockoutFamilyMemberInfo applicantInfo = familyInfo.getMemberMap().get(applicantId);
            if (applicantInfo == null) {
                continue;
            }
            PktAuxFamilyWarApplicant applicant = new PktAuxFamilyWarApplicant(applicantId, applicantInfo.getName(), applicantInfo.getPostId(),
                    applicantInfo.getLevel(), applicantInfo.getFightScore(), familyInfo.getTeamSheet().contains(applicantId) ? K_APP_QUAL_ELITE : K_APP_QUAL_NORMAL);
            applicants.put(applicantId, applicant);
        }
        FamilyWarRpcHelper.familyWarService().sendApplicationSheet(mainServerId, roleId, new HashMap<>(applicants), packet);
    }

    public void applyEliteFightSheet(int mainServerId, long familyId, long roleId) {
        // 判断阶段
        // TODO: 2017-05-04 改名单
        if (!canApply(FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW)) {
            roleService().warn(mainServerId, roleId, DataManager.getGametext("familywar_tips_cantchange"));
            return;
        }
        KnockoutFamilyInfo familyInfo = familyMap.get(familyId);
        if (familyInfo == null) {
            roleService().warn(mainServerId, roleId, "报名失败，您的家族不在参战家族名单里");
            return;
        }
        if (!familyInfo.getMemberMap().containsKey(roleId)) {
            roleService().warn(mainServerId, roleId, "报名失败");
            return;
        }
        familyInfo.getApplicationSheet().add(roleId);
        sendApplicationSheet(mainServerId, familyId, roleId);
    }

    public void cancelApplyEliteFightSheet(int mainServerId, long familyId, long roleId) {
        // 判断阶段
        if (!canApply(FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW)) {
            roleService().warn(mainServerId, roleId, DataManager.getGametext("familywar_tips_cantchange"));
            return;
        }
        KnockoutFamilyInfo familyInfo = familyMap.get(familyId);
        if (familyInfo == null) {
            roleService().warn(mainServerId, roleId, "apply not ok: no such family");
            return;
        }
        if (!familyInfo.getMemberMap().containsKey(roleId)) {
            roleService().warn(mainServerId, roleId, "apply not ok: no such member");
            return;
        }
        familyInfo.getApplicationSheet().remove(roleId);
        if (familyInfo.getTeamSheet().contains(roleId)) {
            familyInfo.getTeamSheet().remove(roleId);
        }
        String name = familyInfo.getMemberMap().get(roleId).getName();
        FamilyWarRpcHelper.familyWarService().sendEmailToSingle(mainServerId, familyInfo.getMasterId(), emailTemplateIdOfCancelFromTeamSheet, 0L, "系统", null, name);
        sendApplicationSheet(mainServerId, familyId, roleId);
    }

    public void confirmTeamSheet(int mainServerId, long familyId, long verifierId, Set<Long> newTeamSheet) {
        // 判断阶段
        if (!canApply(FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW)) {
            roleService().warn(mainServerId, verifierId, DataManager.getGametext("familywar_tips_cantchange"));
            return;
        }
        if (newTeamSheet.size() > FamilyActWarManager.numOfFighterInEliteFight) {
            roleService().warn(mainServerId, verifierId, "参赛人数不能多于5个");
            return;
        }
        KnockoutFamilyInfo familyInfo = familyMap.get(familyId);
        if (familyInfo == null) {
            roleService().warn(mainServerId, verifierId, "confirm not ok: no such family");
            return;
        }
        if (failFamilySet.contains(familyId)) {
            roleService().warn(mainServerId, verifierId, "家族已被淘汰，无需设置名单");
            return;
        }
        KnockoutFamilyMemberInfo verifierInfo = familyInfo.getMemberMap().get(verifierId);
        if (verifierInfo == null) {
            roleService().warn(mainServerId, verifierId, "confirm not ok: no such member");
            return;
        }
        // fixme: 去掉权限控制
        if (verifierInfo.getPostId() == FamilyPost.MASSES_ID || verifierInfo.getPostId() == FamilyPost.MEMBER_ID) {
            roleService().warn(mainServerId, verifierId, "confirm not ok: no such power");
            return;
        }
        Set<Long> oldTeamSheet = familyInfo.getTeamSheet();
        Set<Long> addTeamSheet = union(newTeamSheet, oldTeamSheet);
        addTeamSheet.removeAll(oldTeamSheet);
        Set<Long> delTeamSheet = union(newTeamSheet, oldTeamSheet);
        delTeamSheet.removeAll(newTeamSheet);

        familyInfo.getTeamSheet().clear();
        familyInfo.getTeamSheet().addAll(newTeamSheet);
        roleService().warn(mainServerId, verifierId, "参赛名单修改成功");
        sendMainIcon(mainServerId, verifierId, familyId, FamilyWarConst.STATE_ICON_DISAPPEAR, 0, FamilyWarConst.W_TYPE_QUALIFYING);
        hasNoticeMasterMap.put(familyInfo.getMasterId(), true);
        StringBuilder roleNames = new StringBuilder();
        KnockoutFamilyMemberInfo memberInfo;
        for (long roleId : newTeamSheet) {
            memberInfo = familyInfo.getMemberMap().get(roleId);
            if (memberInfo == null) continue;
            roleNames.append(memberInfo.getName()).append("\\n");
        }
        String familyMsg = String.format(DataManager.getGametext("familywar_desc_familywarchat"), roleNames.toString());
        FamilyWarRpcHelper.familyWarService().chat(mainServerId, "系统", ChatManager.CHANNEL_FAMILY, 0L, familyId, familyMsg, false);
        ServiceHelper.familyWarQualifyingService().sendTeamSheetChangedEmail(FamilyWarUtil.getFamilyWarServerId(), mainServerId, familyId, addTeamSheet, delTeamSheet);
    }

    /* 发送赛程 */
    public void sendFixtures(int mainServerId, long familyId, long roleId, long fightScore) {
        // 判断阶段/状态
        byte selfQualification = ClientFamilyWarUiFixtures.Q_NO;
        KnockoutFamilyInfo familyInfo = familyMap.get(familyId);
        FamilyWarKnockoutBattle battle = null;
        if (familyInfo != null) {
            if (familyInfo.getTeamSheet().contains(roleId)) {
                selfQualification = ClientFamilyWarUiFixtures.Q_ELITE;
            } else {
                selfQualification = ClientFamilyWarUiFixtures.Q_NORMAL;
            }
            battle = battleMap.get(familyInfo.getBattleId());
        } else {
            List<KnockoutFamilyInfo> infos = new LinkedList<>();
            infos.addAll(familyMap.values());
            Collections.sort(infos);
            ClientFamilyWarUiFixtures fixtures = new ClientFamilyWarUiFixtures(ClientFamilyWarUiFixtures.SUBTYPE_QUALIFY_NONE);
            fixtures.setWarType(ClientFamilyWarUiFixtures.T_QUALIFY);
            fixtures.setSelfFamilyFightScore(fightScore);
            fixtures.setFamilyWarMinFightScore(Collections.min(infos).getTotalFightScore());
            fixtures.setDeadLine(FamilyWarUtil.getBattleTimeL(FamilyWarQualifyingFlow.STEP_START_QUALIFYING, ActConst.ID_FAMILY_WAR_QUALIFYING));
            fixtures.setFamilyNameList(getFamilyNames(infos, mainServerId));
            fixtures.setFamilySize((byte) familyMap.size());
            roleService().send(mainServerId, roleId, fixtures);
            return;
        }
        ClientFamilyWarUiFixtures packet = new ClientFamilyWarUiFixtures(ClientFamilyWarUiFixtures.SUBTYPE_ALL);
        packet.setWarState(ClientFamilyWarUiFixtures.S_PREPARE);
        int thisStep = FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW;
        LogUtil.info("familywar|state:{}", packet.getWarState());
        String text = "";
        switch (thisStep) {
            case FamilyWarQualifyingFlow.STEP_GENERATE_TEAM_SHEET:
                packet.setWarState(ClientFamilyWarUiFixtures.S_SHEET);
                LogUtil.info("familywar|state:{}", packet.getWarState());
                break;
            case FamilyWarQualifyingFlow.STEP_BEFORE_1ST:
            case FamilyWarQualifyingFlow.STEP_BEFORE_2ND:
            case FamilyWarQualifyingFlow.STEP_BEFORE_3RD:
            case FamilyWarQualifyingFlow.STEP_BEFORE_4TH:
            case FamilyWarQualifyingFlow.STEP_BEFORE_5TH:
                packet.setWarState(ClientFamilyWarUiFixtures.S_SHOW_ICON);
                LogUtil.info("familywar|state:{}", packet.getWarState());
                break;
            case FamilyWarQualifyingFlow.STEP_START_1ST:
            case FamilyWarQualifyingFlow.STEP_START_2ND:
            case FamilyWarQualifyingFlow.STEP_START_3RD:
            case FamilyWarQualifyingFlow.STEP_START_4TH:
            case FamilyWarQualifyingFlow.STEP_START_5TH:
                packet.setWarState(ClientFamilyWarUiFixtures.S_ELITE);
                LogUtil.info("familywar|state:{}", packet.getWarState());
                break;
            case FamilyWarQualifyingFlow.STEP_START_QUALIFYING:
            case FamilyWarQualifyingFlow.STEP_END_1ST:
            case FamilyWarQualifyingFlow.STEP_END_2ND:
            case FamilyWarQualifyingFlow.STEP_END_3RD:
            case FamilyWarQualifyingFlow.STEP_END_4TH:
            case FamilyWarQualifyingFlow.STEP_END_5TH:
            case FamilyWarQualifyingFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_2ND:
            case FamilyWarQualifyingFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_3RD:
            case FamilyWarQualifyingFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_4TH:
            case FamilyWarQualifyingFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_5TH:
                text = String.format(DataManager.getGametext("familywar_tips_nexturn"), FamilyWarUtil.getNearBattleTimeStr(ActConst.ID_FAMILY_WAR_QUALIFYING));
                packet.setWarState(ClientFamilyWarUiFixtures.S_BETWEEN_ELITE);
                LogUtil.info("familywar|state:{}", packet.getWarState());
                break;
        }
        boolean isEliteFinish = false;
        if (battle != null) {
            isEliteFinish = battle.isEliteFinish();
            if (isEliteFinish) {
                text = DataManager.getGametext("familywar_desc_eliteover");
                packet.setWarState(ClientFamilyWarUiFixtures.S_ELITE_END);
                LogUtil.info("familywar|state:{}", packet.getWarState());
            }
        }
        if (FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW == FamilyWarQualifyingFlow.STEP_END_QUALIFYING) {
            packet.setWarState(ClientFamilyWarUiFixtures.S_CYCLE_END);
            LogUtil.info("familywar|state:{}", packet.getWarState());
        }
        packet.setWarType(ClientFamilyWarUiFixtures.T_QUALIFY);
        packet.setDate(0);
        packet.setText(text);
        int nextBattleRemainderTime = 0;
        if (FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW == FamilyWarQualifyingFlow.STEP_GENERATE_TEAM_SHEET
                || FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW == FamilyWarQualifyingFlow.STEP_END_1ST
                || FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW == FamilyWarQualifyingFlow.STEP_END_2ND
                || FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW == FamilyWarQualifyingFlow.STEP_END_3RD
                || FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW == FamilyWarQualifyingFlow.STEP_END_4TH
                || FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW == FamilyWarQualifyingFlow.STEP_END_5TH) {
            nextBattleRemainderTime = (int) ((FamilyWarUtil.getNearBattleTimeL(ActConst.ID_FAMILY_WAR_QUALIFYING) - System.currentTimeMillis()) / 1000);
        }
        packet.setNextBattleRemainderTime(nextBattleRemainderTime);
        packet.setPlayerQualification(selfQualification);
        packet.setFamilyQualification((byte) (familyInfo == null ? 0 : familyInfo.isWinner() ? 1 : 0));
        packet.setSelfFamilyId(Long.toString(familyId));
        packet.setSelffamilyName(familyMap.get(familyId).getFamilyName());
        IndexList rankList = familyWinPointsMap.get(familyIdToGroupId.get(familyId));
        packet.setSelfRank(rankList != null ? rankList.getRank(Long.toString(familyId)) : 0);
        packet.setSelfPoints(rankList != null ? rankList.getRankObjByKey(Long.toString(familyId)).getPoints() : 0L);
        LogUtil.info("familywar|myFamilyId:{},contain:{} familyIds:{}", familyId, familyIdCacheList.containsKey(familyId), familyIdCacheList.keySet());
        List<FamilyWarQualifyingFixtureCache> cacheList = familyIdCacheList.get(familyId);
        for (FamilyWarQualifyingFixtureCache cache : cacheList) {
            long opFamilyId = getOpponentFamilyId(cache, familyId);
            packet.addOpponent(new PktAuxQualifyFamilyWarOpponent(cache.getBattleType(), opFamilyId, MultiServerHelper.getServerName(familyMap.get(opFamilyId).getMainServerId()),
                    familyMap.get(opFamilyId).getFamilyName(), FamilyWarUtil.getBattleTimeLByType(cache.getBattleType()), winOrLose(cache, familyId)));
        }
        roleService().send(mainServerId, roleId, packet);
    }

    private List<String> getFamilyNames(List<KnockoutFamilyInfo> infos, int mainServerId) {
        List<String> tmpString = new ArrayList<>();
        for (KnockoutFamilyInfo info : infos) {
            if (info.getMainServerId() == mainServerId) {
                tmpString.add(info.getFamilyName());
            }
        }
        return tmpString;
    }

    public void sendFamilyRankObj(int fromServerId, long familyId, long roleId) {
        int groupId = familyIdToGroupId.get(familyId);
        IndexList rankList = familyWinPointsMap.get(groupId);
        if (rankList == null) {
            ClientFamilyWarPointsRank pointsRank = new ClientFamilyWarPointsRank();
            pointsRank.setPointsObjs(new ArrayList<PkAuxQualifyFamilyWarPointsObj>());
            roleService().send(fromServerId, roleId, pointsRank);
            return;
        }
        List<RankObj> rankObjList = rankList.getAll();
        List<PkAuxQualifyFamilyWarPointsObj> pointsObjs = new ArrayList<>();
        for (RankObj rankObj : rankObjList) {
            if (rankObj == null) continue;
            pointsObjs.add(createPointsRankObj(rankList.getRank(rankObj.getKey()), rankObj));
        }
        ClientFamilyWarPointsRank pointsRank = new ClientFamilyWarPointsRank();
        pointsRank.setPointsObjs(pointsObjs);
        roleService().send(fromServerId, roleId, pointsRank);
    }

    private PkAuxQualifyFamilyWarPointsObj createPointsRankObj(int rank, RankObj obj) {
        if (obj == null) return null;
        QualifyingVictoryRankObj rankObj = (QualifyingVictoryRankObj) obj;
        return new PkAuxQualifyFamilyWarPointsObj(rank, rankObj.getKey(), rankObj.getFamilyName(),
                rankObj.getVictoryCount(), rankObj.getDefeatCount(), rankObj.getPoints());
    }


    private byte winOrLose(FamilyWarQualifyingFixtureCache cache, long familyId) {
        if (cache.getWinnerFamilyId() == 0L) {
            return FamilyWarConst.none;
        }
        if (cache.getWinnerFamilyId() == familyId) {
            return FamilyWarConst.win;
        } else {
            return FamilyWarConst.lose;
        }
    }

    private long getOpponentFamilyId(FamilyWarQualifyingFixtureCache cache, long familyId) {
        if (familyId == cache.getCamp1FamilyId()) {
            return cache.getCamp2FamilyId();
        } else {
            return cache.getCamp1FamilyId();
        }
    }

    private Set<Long> union(Set<Long> s1, Set<Long> s2) {
        Set<Long> set = new HashSet<>();
        set.addAll(s1);
        set.addAll(s2);
        return set;
    }

    private boolean canApply(int step) {
        switch (step) {
            case FamilyWarQualifyingFlow.STEP_START_QUALIFYING:
            case FamilyWarQualifyingFlow.STEP_END_1ST:
            case FamilyWarQualifyingFlow.STEP_END_2ND:
            case FamilyWarQualifyingFlow.STEP_END_3RD:
            case FamilyWarQualifyingFlow.STEP_END_4TH:
            case FamilyWarQualifyingFlow.STEP_END_5TH:
            case FamilyWarQualifyingFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_2ND:
            case FamilyWarQualifyingFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_3RD:
            case FamilyWarQualifyingFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_4TH:
            case FamilyWarQualifyingFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_5TH:
                return true;
            default:
                return false;
        }
    }

    /**
     * 请求点赞
     *
     * @param mainServerId
     * @param roleId
     * @param familyId
     */
    public void reqSupport(int mainServerId, long roleId, long familyId) {
        KnockoutFamilyInfo familyInfo = familyMap.get(familyId);
        if (familyInfo == null) {
            roleService().warn(mainServerId, roleId, "support error : no such family");
            return;
        }
        roleService().notice(mainServerId, roleId, new FamilyWarSupportEvent(FamilyWarConst.W_TYPE_QUALIFYING));
    }

    /**
     * 增加家族被点赞次数
     *
     * @param mainServerId
     * @param roleId
     * @param familyId
     */
    public void addSupport(int mainServerId, long roleId, long familyId) {
        KnockoutFamilyInfo familyInfo = familyMap.get(familyId);
        if (familyInfo == null) return;
        familyInfo.addSupport();
        roleService().send(mainServerId, roleId, new ClientFamilyWarUiSupport(familyId));
    }

    public void AsyncFihterEntityAndLockFamily() {
        Map<Integer, Set<Long>> roleServerMap = new HashMap<>();
        for (KnockoutFamilyInfo info : familyMap.values()) {
            Set<Long> roleSet = roleServerMap.get(info.getMainServerId());
            if (roleSet == null) {
                roleSet = new HashSet<>();
                roleServerMap.put(info.getMainServerId(), roleSet);
            }
            roleSet.addAll(info.getMemberMap().keySet());
            FamilyWarRpcHelper.familyWarService().lockFamily(info.getMainServerId(), info.getFamilyId());
        }
        for (Map.Entry<Integer, Set<Long>> entry : roleServerMap.entrySet()) {
            FamilyWarRpcHelper.familyWarService().updateFighterEntity(entry.getKey(), FamilyWarConst.W_TYPE_QUALIFYING, entry.getValue());
        }
    }

    public void updateFighterEntity(Map<Long, FighterEntity> entityMap) {
        for (Map.Entry<Long, FighterEntity> entry : entityMap.entrySet()) {
            try {
                familyMap.get(memberMap.get(entry.getKey()).getFamilyId()).getMemberMap().get(entry.getKey()).setFighterEntity(entry.getValue());
                roleService().notice(getMainServerId(entry.getKey()), entry.getKey(), new FamilyWarFightingOrNotEvent(true));
            } catch (Exception e) {
                LogUtil.info("roleId:{}, contain:{}, memberKeySet:{}", entry.getKey(), memberMap.containsKey(entry.getKey()), memberMap.keySet());
                e.printStackTrace();
            }
        }
        LogUtil.info("familywar|跨服海选 玩家 {} 战斗实体同步完毕", entityMap.keySet());
    }

    /**
     * 获得复活状态，是否复活，0=否 1=是
     *
     * @param battleId
     * @param fightId
     * @param fighterUid
     * @return
     */
    public byte getFighterReviveState(String battleId, String fightId, String fighterUid) {
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle == null) return 0;
        return battle.getFighterReviveState(fighterUid, fightId);
    }

    public void containFamily(int fromServerId, long familyId, long roleId) {
        byte state;
        if (familyMap.containsKey(familyId)) {
            state = FamilyWarConst.havQulification;
        } else {
            state = FamilyWarConst.noneQulification;
        }
        FamilyWarRpcHelper.familyWarService().containFamily(fromServerId, familyId, roleId, state);
    }

    public Map<String, FamilyWarKnockoutBattle> getBattleMap() {
        return battleMap;
    }

    public Map<Long, KnockoutFamilyMemberInfo> getMemberMap() {
        return memberMap;
    }

}
