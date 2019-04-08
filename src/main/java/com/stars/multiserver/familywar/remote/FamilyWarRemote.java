package com.stars.multiserver.familywar.remote;

import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.modules.familyactivities.war.event.FamilyWarEnterSafeSceneEvent;
import com.stars.modules.familyactivities.war.event.FamilyWarFightingOrNotEvent;
import com.stars.modules.familyactivities.war.packet.ClientFamilyWarBattleFightPersonalPoint;
import com.stars.modules.familyactivities.war.packet.ui.ClientFamilyWarUiApply;
import com.stars.modules.familyactivities.war.packet.ui.ClientFamilyWarUiFixtures;
import com.stars.modules.familyactivities.war.packet.ui.ClientFamilyWarUiMinPointsAward;
import com.stars.modules.familyactivities.war.packet.ui.ClientFamilyWarUiSupport;
import com.stars.modules.familyactivities.war.packet.ui.auxiliary.PktAuxFamilyWarApplicant;
import com.stars.modules.familyactivities.war.packet.ui.auxiliary.PktAuxFamilyWarFamilyInfo;
import com.stars.modules.familyactivities.war.prodata.FamilyWarRankAwardVo;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.serverLog.EventType;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.*;
import com.stars.multiserver.familywar.event.FamilyWarSupportEvent;
import com.stars.multiserver.familywar.flow.FamilyWarRemoteFlow;
import com.stars.multiserver.familywar.knockout.FamilyWarKnockoutBattle;
import com.stars.multiserver.familywar.knockout.KnockoutFamilyInfo;
import com.stars.multiserver.familywar.knockout.KnockoutFamilyMemberInfo;
import com.stars.multiserver.familywar.knockout.fight.elite.FamilyWarEliteFightStat;
import com.stars.multiserver.fight.FightIdCreator;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.services.activities.ActConst;
import com.stars.services.chat.ChatManager;
import com.stars.services.family.FamilyConst;
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

/**
 * 跨服流程处理
 * Created by chenkeyu on 2017-04-27 19:53
 */
public class FamilyWarRemote extends FamilyWar {
    private int startBattleType;
    private int battleType;
    private int sendAwardBattleType;
    private Map<Integer, Map<Integer, List<Long>>> groupOfFamilyMap;//battleType,<groupId , list of familyId>
    private Map<Long, Integer> familyIdToGroupId;//familyId,groupId
    private Map<Integer, long[]> fixtureMap;//对阵表
    private Map<Integer, Set<Long>> outOfWarFamily;//被淘汰的家族

    private LinkedHashMap<Integer, Long> indexFamilyId;//特蛋疼的家族位置
    /* 战斗表 */
    private Map<String, FamilyWarKnockoutBattle> battleMap; // 战斗表
    private Map<Integer, Map<String, FamilyWarKnockoutBattle>> battleTypeOfBattleMap;

    private Map<Long, Boolean> remoteTipMap;//

    private int isDisaster = -1;
    private long[] disasterFixture;

    /* 名次 */
    private long _1stFamilyId = 0L;
    private long _2ndFamilyId = 0L;
    private long _3rdFamilyId = 0L;
    private long _4thFamilyId = 0L;

    public FamilyWarRemote() {
        battleMap = new HashMap<>();
        battleTypeOfBattleMap = new HashMap<>();
        outOfWarFamily = new HashMap<>();
        remoteTipMap = new HashMap<>();
    }

    public void generateFixture() {
        List<KnockoutFamilyInfo> infoList = new LinkedList<>();
        infoList.addAll(familyMap.values());
        Collections.sort(infoList);
        int lack = getLack(infoList);
        this.startBattleType = infoList.size() + lack;
        this.battleType = FamilyWarConst.R_BATTLE_TYPE_INIT;
        generateGroupOfFamilyMap(infoList, battleType, lack, isDisaster);
    }

    private int getLack(List<KnockoutFamilyInfo> infoList) {
        int lack = 0;
        //保证size的值为32 or 16 or 8;
        if (infoList.size() != 32 || infoList.size() != 16 || infoList.size() != 8) {
            if (infoList.size() > 32) {
                // FIXME: 2017-06-09 取前32
                LogUtil.info("familywar|家族个数超了 size:{}", infoList.size());
            }
            if (infoList.size() > 16 && infoList.size() < 32) {
                lack = 32 - infoList.size();
            }
            if (infoList.size() > 8 && infoList.size() < 16) {
                lack = 16 - infoList.size();
            }
            if (infoList.size() > 4 && infoList.size() < 8) {
                lack = 8 - infoList.size();
            }
            if (infoList.size() > 0 && infoList.size() < 4) {
                lack = 4 - infoList.size();
            }
        }
        LogUtil.info("familywar|获取缺失的家族个数 lack:{} ,家族数量:{}, 理论上来说，lack值为0", lack, infoList.size());
        return lack;
    }

    private void generateGroupOfFamilyMap(List<KnockoutFamilyInfo> infoList, int preBattleType, int lack, int isDisaster) {
        if (isDisaster == -1) {
            this.battleType = FamilyWarUtil.getNextBattleType(preBattleType, infoList.size() + lack);
        } else {
            this.battleType = FamilyWarUtil.getNextBattleType(isDisaster, infoList.size() + lack);
            fixtureMap = new HashMap<>();
            fixtureMap.put(1, disasterFixture);
            LogUtil.info("familywar|拉起流程,battleType:{},isDisaster:{}", this.battleType, isDisaster);
        }
        if (battleType == FamilyWarConst.R_BATTLE_TYPE_OVER) {
            return;
        }
        int groupSize = (infoList.size() + lack) / 8;
        if (infoList.size() <= 4) groupSize = 1;
        groupOfFamilyMap = new HashMap<>();
        familyIdToGroupId = new HashMap<>();
        groupOfFamilyMap.put(battleType, new HashMap<Integer, List<Long>>());
        for (int i = 0; i < groupSize; i++) {
            groupOfFamilyMap.get(battleType).put(i + 1, new ArrayList<Long>());
        }
        for (int i = 0; i < infoList.size(); i++) {
            int groupId = (i + groupSize) % groupSize + 1;
            List<Long> familyIdList = groupOfFamilyMap.get(battleType).get(groupId);
            familyIdToGroupId.put(infoList.get(i).getFamilyId(), groupId);
            familyIdList.add(infoList.get(i).getFamilyId());
        }
        if ((battleType == R_BATTLE_TYPE_4TO2 || battleType == R_BATTLE_TYPE_FINAL) && groupSize == 1) {
            List<Long> familyIdList = new ArrayList<>();
            for (int i = 0; i < 16; i++) {
                familyIdList.add(0L);
            }
            long[] fixtureLong = fixtureMap.get(1);
            for (Map.Entry<Integer, Long> entry : indexFamilyId.entrySet()) {
                familyIdList.set(entry.getKey(), entry.getValue());
                fixtureLong[entry.getKey()] = entry.getValue();
            }
            groupOfFamilyMap.get(battleType).put(1, familyIdList);
        }
        if (battleType == R_BATTLE_TYPE_32TO16 || battleType == R_BATTLE_TYPE_16TO8 || battleType == R_BATTLE_TYPE_8TO4) {
            fixtureMap = new HashMap<>();
            generateFixtures(groupOfFamilyMap.get(battleType));
        }
        //补充0L的家族
        for (int i = 0; i < lack; i++) {
            Map<Integer, List<Long>> listMap = groupOfFamilyMap.get(battleType);
            int groupId = (i + groupSize) % groupSize + 1;
            listMap.get(groupId).add(0L);
        }
        Set<Long> outFamily = outOfWarFamily.get(preBattleType);
        LogUtil.info("familywar| 生成下一阶段的对阵表  battleType:{},familyMap:{},fixtureMap:{}", battleType, groupOfFamilyMap.get(battleType), fixtureMap);
        ServiceHelper.familyWarRemoteService().onGenerateFinish(MultiServerHelper.getServerId(), battleType, new HashMap<>(groupOfFamilyMap.get(battleType)),
                indexFamilyId, outFamily);
    }

    /**
     * @param groupOfFamilyListMap
     */
    private void generateFixtures(Map<Integer, List<Long>> groupOfFamilyListMap) {
        Map<Integer, List<KnockoutFamilyInfo>> infoMap = new HashMap<>();
        for (Map.Entry<Integer, List<Long>> entry : groupOfFamilyListMap.entrySet()) {
            List<KnockoutFamilyInfo> infoList = infoMap.get(entry.getKey());
            if (infoList == null) {
                infoList = new ArrayList<>();
                infoMap.put(entry.getKey(), infoList);
            }
            for (Long familyId : entry.getValue()) {
                infoList.add(familyMap.get(familyId));
            }
        }
        for (Map.Entry<Integer, List<KnockoutFamilyInfo>> entry : infoMap.entrySet()) {
            int groupId = entry.getKey();
            long[] fixtures = new long[16];
            List<KnockoutFamilyInfo> infoList = new ArrayList<>(entry.getValue());
            Collections.sort(infoList);
            fixtures[K_SEQ_QUARTER_A] = removeAndRetureFamilyId(infoList);
            fixtures[K_SEQ_QUARTER_E] = removeAndRetureFamilyId(infoList);
            fixtures[K_SEQ_QUARTER_C] = removeAndRetureFamilyId(infoList);
            fixtures[K_SEQ_QUARTER_G] = removeAndRetureFamilyId(infoList);
            // 生成剩余的位置
            List<Integer> tempList = new ArrayList<>();
            tempList.add(K_SEQ_QUARTER_B);
            tempList.add(K_SEQ_QUARTER_D);
            tempList.add(K_SEQ_QUARTER_F);
            tempList.add(K_SEQ_QUARTER_H);
            int size = tempList.size();
            Random random = new Random();
            for (int i = 0; i < size && infoList.size() > 0; i++) {
                long familyId = infoList.remove(0).getFamilyId();
                int idx = random.nextInt(tempList.size());
                int seq = tempList.remove(idx);
                fixtures[seq] = familyId;
            }
            for (int seq = K_SEQ_QUARTER_A; seq <= K_SEQ_QUARTER_H; seq++) {
                long familyId = fixtures[seq];
                KnockoutFamilyInfo info = familyMap.get(familyId);
                if (info == null) continue;
                info.setSeq(seq);
            }
            fixtureMap.put(groupId, fixtures);
        }
        if (battleType == R_BATTLE_TYPE_8TO4) {
            indexFamilyId = new LinkedHashMap<>();
            for (long[] longs : fixtureMap.values()) {
                for (int i = 0; i < longs.length; i++) {
                    indexFamilyId.put(i, longs[i]);
                }
                break;
            }
        }
        for (Map.Entry<Integer, long[]> entry : fixtureMap.entrySet()) {
            LogUtil.info("familywar|决赛 {} 赛程表 groupId:{} ,fixtureMap:{}", battleType, entry.getKey(), entry.getValue());

        }
    }

    private long removeAndRetureFamilyId(List<KnockoutFamilyInfo> infoList) {
        return infoList.remove(0).getFamilyId();
    }

    public void sendMainIconToMaster(KnockoutFamilyInfo familyInfo) {
        LogUtil.info("familywar|给族长发设置名单的icon,失败的family:{},通知与否的集合:{}", failFamilySet, hasNoticeMasterMap);
        sendMainIcon(familyInfo.getMainServerId(), familyInfo.getMasterId(), familyInfo.getFamilyId(), FamilyWarConst.STATE_NOTICE_MASTER, 0L, FamilyWarConst.W_TYPE_QUALIFYING);
    }

    public void startBattle(int battleType) {
        LogUtil.info("familywar|跨服决赛开始战斗:{}   listMap:{}", battleType, fixtureMap);
        if (battleType == FamilyWarConst.R_BATTLE_TYPE_32TO16 || battleType == FamilyWarConst.R_BATTLE_TYPE_16TO8 ||
                battleType == FamilyWarConst.R_BATTLE_TYPE_8TO4) {
            for (Map.Entry<Integer, long[]> entry : fixtureMap.entrySet()) {
                int groupId = entry.getKey();
                long[] fixture = entry.getValue();
                int start = K_SEQ_SEMI_I;
                int end = K_SEQ_SEMI_L;
                for (int i = K_SEQ_SEMI_I; i <= K_SEQ_SEMI_L; i++) {
                    long camp1 = fixture[(i + 1) * 2];
                    long camp2 = fixture[(i + 1) * 2 + 1];
                    LogUtil.info("familywar|跨服决赛对阵双方家族 battleType:{},groupId:{},camp1FamilyId:{},camp2FamilyId:{}", battleType, groupId, camp1, camp2);
                    startBattle(battleType, groupId, camp1, camp2);
                }
            }
        } else {
            Map<Integer, List<Long>> listMap = groupOfFamilyMap.get(battleType);
            if (listMap == null) return;
            for (Map.Entry<Integer, List<Long>> longListMap : listMap.entrySet()) {
                int groupId = longListMap.getKey();
                List<Long> longList = longListMap.getValue();
                int size = longList.size();//list.size() 都是16
                if (battleType == FamilyWarConst.R_BATTLE_TYPE_4TO2) {
                    for (int i = K_SEQ_FINAL_M; i <= K_SEQ_FINAL_N; i++) {
                        startBattle(battleType, groupId, longList.get((i + 1) * 2), longList.get((i + 1) * 2 + 1));
                    }
                } else {
                    startBattle(battleType, groupId, longList.get(K_SEQ_FINAL_M), longList.get(K_SEQ_FINAL_N));
                    startBattle(FamilyWarConst.R_BATTLE_TYPE_3RD4TH, groupId, longList.get(K_SEQ_FINAL_34_O), longList.get(K_SEQ_FINAL_34_P));
                }
            }
        }
        for (FamilyWarKnockoutBattle battle : battleTypeOfBattleMap.get(battleType).values()) {
            battle.start(FamilyWarConst.W_TYPE_REMOTE);
        }
        if (battleType == FamilyWarConst.R_BATTLE_TYPE_FINAL) {
            for (FamilyWarKnockoutBattle battle : battleTypeOfBattleMap.get(FamilyWarConst.R_BATTLE_TYPE_3RD4TH).values()) {
                battle.start(FamilyWarConst.W_TYPE_REMOTE);
            }
        }
        startMatch();
    }

    private void startBattle(int battleType, int groupId, long camp1FamilyId, long camp2FamilyId) {
        if (camp1FamilyId == 0L && camp2FamilyId == 0L) {
            LogUtil.info("familywar|轮空处理 , 双方都轮空 , camp1:{}, camp2:{}", camp1FamilyId, camp2FamilyId);
            return;
        }
        if (camp1FamilyId == 0L) {
            handleEmptyBattle(camp2FamilyId, battleType);
            LogUtil.info("familywar|轮空处理完毕,由于在{} 战斗中 {} 家族轮空，所以 {} 家族直接胜利 ", battleType, camp1FamilyId, camp2FamilyId);
            return;
        }
        if (camp2FamilyId == 0L) {
            handleEmptyBattle(camp1FamilyId, battleType);
            LogUtil.info("familywar|轮空处理完毕,由于在{} 战斗中 {} 家族轮空，所以 {} 家族直接胜利 ", battleType, camp2FamilyId, camp1FamilyId);
            return;
        }
        String battleId = "fwr-" + MultiServerHelper.getServerId() + "-" + battleType + "-" + groupId + "-" + camp1FamilyId + "-" + camp2FamilyId + "-" + FightIdCreator.creatUUId();
        FamilyWarKnockoutBattle battle = new FamilyWarKnockoutBattle(battleId, battleType, familyMap.get(camp1FamilyId), familyMap.get(camp2FamilyId));
        battle.setFamilyWar(this);
        battleMap.put(battleId, battle);
        putToBrachBattleMap(battleType, battleId, battle);
        familyMap.get(camp1FamilyId).setBattleId(battleId);
        familyMap.get(camp2FamilyId).setBattleId(battleId);
    }

    private void handleEmptyBattle(long familyId, int battleType) {
        KnockoutFamilyInfo winnerFamilyInfo = familyMap.get(familyId);
        winnerFamilyInfo.setWinner(true);
    }

    private void putToBrachBattleMap(int battleType, String battleId, FamilyWarKnockoutBattle battle) {
        Map<String, FamilyWarKnockoutBattle> remoteBattleMap = battleTypeOfBattleMap.get(battleType);
        if (remoteBattleMap == null) {
            remoteBattleMap = new HashMap<>();
            battleTypeOfBattleMap.put(battleType, remoteBattleMap);
        }
        remoteBattleMap.put(battleId, battle);
    }

    public void endBattle(int battleType) {
        LogUtil.info("familywar|{}战斗结束:{}", battleType);
        endMatch();
        if (battleType == FamilyWarConst.R_BATTLE_TYPE_FINAL) {
            for (FamilyWarKnockoutBattle battle : battleTypeOfBattleMap.get(FamilyWarConst.R_BATTLE_TYPE_3RD4TH).values()) {
                battle.finishAllFight();
            }
        }
        for (FamilyWarKnockoutBattle battle : battleTypeOfBattleMap.get(battleType).values()) {
            battle.finishAllFight();
        }
        for (FamilyWarKnockoutBattle battle : battleTypeOfBattleMap.get(battleType).values()) {
            battleMap.remove(battle.getBattleId());
        }
        List<KnockoutFamilyInfo> infoList = new LinkedList<>();
        List<Long> roleIds = new ArrayList<>();
        for (KnockoutFamilyInfo familyInfo : familyMap.values()) {
            roleIds.addAll(familyInfo.getTeamSheet());
            if (failFamilySet.contains(familyInfo.getFamilyId()))
                continue;
            hasNoticeMasterMap.put(familyInfo.getMasterId(), false);
            infoList.add(familyInfo);
//            FamilyWarRpcHelper.familyWarService().halfLockFamily(familyInfo.getMainServerId(), familyInfo.getFamilyId());
        }
        Set<Long> unLockFamilySet = outOfWarFamily.get(battleType);
        if (battleType == R_BATTLE_TYPE_FINAL)
            unLockFamilySet.addAll(outOfWarFamily.get(R_BATTLE_TYPE_3RD4TH));
        if (unLockFamilySet != null) {
            Map<Integer, List<Long>> unLockFamilyMap = new HashMap<>();
            for (long familyId : unLockFamilySet) {
                KnockoutFamilyInfo info = familyMap.get(familyId);
                List<Long> familyIds = unLockFamilyMap.get(info.getMainServerId());
                if (familyIds == null) {
                    familyIds = new ArrayList<>();
                    unLockFamilyMap.put(info.getMainServerId(), familyIds);
                }
                familyIds.add(familyId);
            }
            for (Map.Entry<Integer, List<Long>> entry : unLockFamilyMap.entrySet()) {
                FamilyWarRpcHelper.familyWarService().unLockFamily(entry.getKey(), entry.getValue());
            }
        }
        Collections.sort(infoList);
        generateGroupOfFamilyMap(infoList, battleType, getLack(infoList), isDisaster);
        this.sendAwardBattleType = battleType;
        if (battleType == R_BATTLE_TYPE_FINAL) {
            ServiceHelper.familyWarRemoteService().update_1stTO_4thFamily(MultiServerHelper.getServerId(), _1stFamilyId, _2ndFamilyId, _3rdFamilyId, _4thFamilyId);
        }
        if (battleType == R_BATTLE_TYPE_32TO16 || battleType == R_BATTLE_TYPE_16TO8) {
            for (KnockoutFamilyInfo info : familyMap.values()) {
                for (long roleId : info.getMemberMap().keySet()) {
                    remoteTipMap.put(roleId, false);
                }
            }
        }
        try {
            unLockRoleState(roleIds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unLockRoleState(List<Long> roleIds) {
        for (long roleId : roleIds) {
            try {
                roleService().notice(getMainServerId(roleId), roleId, new FamilyWarFightingOrNotEvent(false));
            } catch (Exception e) {
                LogUtil.info("familywar|通知玩家 {} 解除家族战战斗中的锁失败", roleId);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void finishBattle(String battleId, long winnerFamilyId, long loserFamilyId) {
        FamilyWarKnockoutBattle battle = battleMap.remove(battleId);
        if (battle == null) {
            LogUtil.error("familywar|不存在对战, battleId=" + battleId);
            return;
        }
        int battleType = battle.getType();
        KnockoutFamilyInfo winnerFamilyInfo = familyMap.get(winnerFamilyId);
        KnockoutFamilyInfo loserFamilyInfo = familyMap.get(loserFamilyId);
        winnerFamilyInfo.setWinner(true);
        if (battleType == FamilyWarConst.R_BATTLE_TYPE_8TO4) {
            int nextSeq = winnerFamilyInfo.getSeq() / 2 - 1;
            winnerFamilyInfo.setSeq(nextSeq);
            loserFamilyInfo.setWinner(false);
            indexFamilyId.put(nextSeq, winnerFamilyId);
            failFamilySet.add(loserFamilyId);
            addFailFamilySetByBattleType(loserFamilyId, battleType);
        } else if (battleType == FamilyWarConst.R_BATTLE_TYPE_4TO2) {
            int nextSeq = winnerFamilyInfo.getSeq() / 2 - 1;
            winnerFamilyInfo.setSeq(nextSeq);
            loserFamilyInfo.setWinner(true);
            loserFamilyInfo.setSeq(nextSeq + 14);
            indexFamilyId.put(nextSeq, winnerFamilyId);
            indexFamilyId.put(nextSeq + 14, loserFamilyId);
        } else if (battleType == FamilyWarConst.R_BATTLE_TYPE_FINAL) {
            _1stFamilyId = winnerFamilyId;
            _2ndFamilyId = loserFamilyId;
            winnerFamilyInfo.setWinner(false);
            loserFamilyInfo.setWinner(false);
            failFamilySet.add(winnerFamilyId);
            failFamilySet.add(loserFamilyId);
            addFailFamilySetByBattleType(winnerFamilyId, battleType);
            addFailFamilySetByBattleType(loserFamilyId, battleType);
            String message = String.format(FamilyActWarManager.familywar_roll_winer3, MultiServerHelper.getServerName(winnerFamilyInfo.getMainServerId()), winnerFamilyInfo.getFamilyName());
            ServiceHelper.familyWarRemoteService().chat(MultiServerHelper.getServerId(), message);
        } else if (battleType == FamilyWarConst.R_BATTLE_TYPE_3RD4TH) {
            _3rdFamilyId = winnerFamilyId;
            _4thFamilyId = loserFamilyId;
            winnerFamilyInfo.setWinner(false);
            loserFamilyInfo.setWinner(false);
            failFamilySet.add(winnerFamilyId);
            failFamilySet.add(loserFamilyId);
            addFailFamilySetByBattleType(winnerFamilyId, battleType);
            addFailFamilySetByBattleType(loserFamilyId, battleType);
        } else {
            loserFamilyInfo.setWinner(false);
            failFamilySet.add(loserFamilyId);
            addFailFamilySetByBattleType(loserFamilyId, battleType);
        }
    }

    private void addFailFamilySetByBattleType(long familyId, int battleType) {
        Set<Long> familyIds = outOfWarFamily.get(battleType);
        if (familyIds == null) {
            familyIds = new HashSet<>();
            outOfWarFamily.put(battleType, familyIds);
        }
        familyIds.add(familyId);
    }

    /**
     * 开启普通赛匹配线程
     */
    public void startMatch() {
        if (FamilyActWarManager.matchScheduler == null) {
            FamilyActWarManager.matchScheduler = Executors.newSingleThreadScheduledExecutor();
            FamilyActWarManager.matchScheduler.scheduleAtFixedRate(FamilyActWarManager.matchTaskRemote,
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

    public void viewMinPointsAward(int mainServerId, long roleId) {
        byte minAwardType = MIN_AWARD_REMOTE_ELITE;
        Long points;
        Set<Long> recordSet = null;
        if (elitePointsMap.containsKey(Long.toString(roleId))) {
            minAwardType = MIN_AWARD_REMOTE_ELITE;
            points = elitePointsMap.get(Long.toString(roleId));
            recordSet = eliteMinPointsAwardAcquiredRecordSet.get(roleId);
        } else if (normalPointsMap.containsKey(Long.toString(roleId))) {
            minAwardType = MIN_AWARd_REMOTE_NORMAL;
            points = normalPointsMap.get(Long.toString(roleId));
            recordSet = normalMinPointsAwardAcquiredRecordSet.get(roleId);
        } else {
            points = null;
        }
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
                    elitePointsMap, recordSet, eliteMinPointsAwardMapForRemote, FamilyWarConst.MIN_AWARD_REMOTE_ELITE);
        } else if (normalPointsMap.containsKey(Long.toString(roleId))) {
            Set<Long> recordSet = normalMinPointsAwardAcquiredRecordSet.get(roleId);
            if (recordSet == null) {
                recordSet = new HashSet<>();
                normalMinPointsAwardAcquiredRecordSet.put(roleId, recordSet);
            }
            acquireMinPointsAward0(mainServerId, roleId, acquirePoints,
                    normalPointsMap, recordSet, normalMinPointsAwardMapForRemote, FamilyWarConst.MIN_AWARd_REMOTE_NORMAL);
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

    public void applyEliteFightSheet(int mainServerId, long familyId, long roleId) {
        // 判断阶段
        // TODO: 2017-05-04 改名单
        if (!canApply(FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW)) {
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
        if (!canApply(FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW)) {
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

    public void confirmTeamSheet(int mainServerId, long familyId, long verifierId, Set<Long> newTeamSheet) {
        // 判断阶段
        if (!canApply(FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW)) {
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
        sendMainIcon(mainServerId, verifierId, familyId, FamilyWarConst.STATE_ICON_DISAPPEAR, 0, FamilyWarConst.W_TYPE_REMOTE);
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
        ServiceHelper.familyWarRemoteService().sendTeamSheetChangedEmail(FamilyWarUtil.getFamilyWarServerId(), mainServerId, familyId, addTeamSheet, delTeamSheet);
    }

    private Set<Long> union(Set<Long> s1, Set<Long> s2) {
        Set<Long> set = new HashSet<>();
        set.addAll(s1);
        set.addAll(s2);
        return set;
    }

    private boolean canApply(int step) {
        switch (step) {
            case FamilyWarRemoteFlow.STEP_START_REMOTE:
            case FamilyWarRemoteFlow.STEP_END_32TO16:
            case FamilyWarRemoteFlow.STEP_END_16TO8:
            case FamilyWarRemoteFlow.STEP_END_8TO4:
            case FamilyWarRemoteFlow.STEP_END_4TO2:
            case FamilyWarRemoteFlow.STEP_END_FINNAL:
            case FamilyWarRemoteFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_4TO2:
            case FamilyWarRemoteFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_8TO4:
            case FamilyWarRemoteFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_16TO8:
            case FamilyWarRemoteFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_FINNAL:
                return true;
            default:
                return false;
        }
    }

    public void sendFixtures(int mainServerId, long familyId, long roleId) {
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
        }
        ClientFamilyWarUiFixtures packet = new ClientFamilyWarUiFixtures(ClientFamilyWarUiFixtures.SUBTYPE_ALL);
        packet.setWarState(ClientFamilyWarUiFixtures.S_PREPARE);
        int thisStep = FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW;
        LogUtil.info("familywar|state:{}", packet.getWarState());
        String text = "";
        switch (thisStep) {
            case FamilyWarRemoteFlow.STEP_GENERATE_TEAM_SHEET:
                packet.setWarState(ClientFamilyWarUiFixtures.S_SHEET);
                LogUtil.info("familywar|state:{}", packet.getWarState());
                break;
            case FamilyWarRemoteFlow.STEP_BEFORE_FINNAL:
            case FamilyWarRemoteFlow.STEP_BEFORE_32TO16:
            case FamilyWarRemoteFlow.STEP_BEFORE_16TO8:
            case FamilyWarRemoteFlow.STEP_BEFORE_8TO4:
            case FamilyWarRemoteFlow.STEP_BEFORE_4TO2:
                packet.setWarState(ClientFamilyWarUiFixtures.S_SHOW_ICON);
                LogUtil.info("familywar|state:{}", packet.getWarState());
                break;
            case FamilyWarRemoteFlow.STEP_START_FINNAL:
            case FamilyWarRemoteFlow.STEP_START_32TO16:
            case FamilyWarRemoteFlow.STEP_START_8TO4:
            case FamilyWarRemoteFlow.STEP_START_16TO8:
            case FamilyWarRemoteFlow.STEP_START_4TO2:
                packet.setWarState(ClientFamilyWarUiFixtures.S_ELITE);
                LogUtil.info("familywar|state:{}", packet.getWarState());
                break;
            case FamilyWarRemoteFlow.STEP_START_REMOTE:
            case FamilyWarRemoteFlow.STEP_END_32TO16:
            case FamilyWarRemoteFlow.STEP_END_16TO8:
            case FamilyWarRemoteFlow.STEP_END_8TO4:
            case FamilyWarRemoteFlow.STEP_END_4TO2:
            case FamilyWarRemoteFlow.STEP_END_FINNAL:
            case FamilyWarRemoteFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_4TO2:
            case FamilyWarRemoteFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_8TO4:
            case FamilyWarRemoteFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_16TO8:
            case FamilyWarRemoteFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_FINNAL:
                text = String.format(DataManager.getGametext("familywar_tips_nexturn"), FamilyWarUtil.getNearBattleTimeStr(ActConst.ID_FAMILY_WAR_REMOTE));
                packet.setWarState(ClientFamilyWarUiFixtures.S_BETWEEN_ELITE);
                LogUtil.info("familywar|state:{}", packet.getWarState());
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
        if (FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW == FamilyWarRemoteFlow.STEP_END_REMOTE) {
            packet.setWarState(ClientFamilyWarUiFixtures.S_CYCLE_END);
            LogUtil.info("familywar|state:{}", packet.getWarState());
        }
        packet.setWarType(ClientFamilyWarUiFixtures.T_REMOTE);
        packet.setDate(0);
        packet.setText(text);
        int nextBattleRemainderTime = 0;
        switch (FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW) {
            case FamilyWarRemoteFlow.STEP_GENERATE_TEAM_SHEET:
            case FamilyWarRemoteFlow.STEP_END_4TO2:
            case FamilyWarRemoteFlow.STEP_END_8TO4:
            case FamilyWarRemoteFlow.STEP_END_16TO8:
            case FamilyWarRemoteFlow.STEP_END_32TO16:
            case FamilyWarRemoteFlow.STEP_END_FINNAL:
                nextBattleRemainderTime = (int) ((FamilyWarUtil.getNearBattleTimeL(ActConst.ID_FAMILY_WAR_REMOTE) - System.currentTimeMillis()) / 1000);
                break;
        }
        packet.setStartBattleType(startBattleType);
        packet.setBattleType(battleType == R_BATTLE_TYPE_OVER ? R_BATTLE_TYPE_FINAL : battleType);
        int timeStep = 0;
        List<Integer> timeLinePoint = FamilyActWarManager.remoteTimeLinePoint.get(startBattleType);
        LogUtil.info("familywar|时间点 type:{},timeLine:{}", startBattleType, timeLinePoint);
        for (int i = 0; i < timeLinePoint.size(); i++) {
            if (FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW >= timeLinePoint.get(i)) {
                timeStep = i;
            }
        }
        packet.set1stFamilyId(_1stFamilyId);
        packet.set2ndFamilyId(_2ndFamilyId);
        packet.set3rdFamilyId(_3rdFamilyId);
        packet.set4thFamilyId(_4thFamilyId);
        packet.setIndexOfTimeline(timeStep + 1);
        packet.setFixtureMap(fixtureMap);
        packet.setNextBattleRemainderTime(nextBattleRemainderTime);
        packet.setPlayerQualification(selfQualification);
        packet.setFamilyQualification((byte) (familyInfo == null ? 0 : familyInfo.isWinner() ? 1 : 0));
        packet.setSelfFamilyGroupId(familyIdToGroupId.containsKey(familyId) ? familyIdToGroupId.get(familyId) : 0);
        for (Map.Entry<Integer, List<Long>> entry : groupOfFamilyMap.get(battleType == R_BATTLE_TYPE_OVER ? R_BATTLE_TYPE_FINAL : battleType).entrySet()) {
            for (long id : entry.getValue()) {
                if (id == 0L) continue;
                KnockoutFamilyInfo info = familyMap.get(id);
                PktAuxFamilyWarFamilyInfo warInfo = new PktAuxFamilyWarFamilyInfo(id, info.getFamilyName(), info.getMainServerId());
                warInfo.setServerName(MultiServerHelper.getServerName(info.getMainServerId()));
                warInfo.setSeq(info.getSeq());
                warInfo.setGroupId(entry.getKey());
                packet.addFamilyInfo(warInfo);
            }
        }
        roleService().send(mainServerId, roleId, packet);
        if ((battleType == R_BATTLE_TYPE_16TO8 || battleType == R_BATTLE_TYPE_8TO4) && (battleType != startBattleType)) {
            ClientFamilyWarUiFixtures tips = new ClientFamilyWarUiFixtures(ClientFamilyWarUiFixtures.SUBTYPE_TIPS);
            if (familyInfo != null && !remoteTipMap.get(roleId)) {
                String tmp = "";
                if (familyInfo.isWinner()) {
                    tmp = String.format(DataManager.getGametext("familywar_desc_momentwin"), FamilyWarUtil.getBattleTypeName(FamilyWarUtil.getPreBattleType(battleType)),
                            FamilyWarUtil.getBattleTypeName(battleType));
                } else {
                    tmp = String.format(DataManager.getGametext("familywar_desc_momentlost"), FamilyWarUtil.getBattleTypeName(FamilyWarUtil.getPreBattleType(battleType)),
                            FamilyWarUtil.getBattleTypeName(battleType));
                }
                remoteTipMap.put(roleId, true);
                tips.setTipText(tmp);
                roleService().send(mainServerId, roleId, tips);
            }
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

    public void checkAndEndTimeout() {
        for (FamilyWarKnockoutBattle battle : battleMap.values()) {
            battle.checkAndHandleTimeoutNormalFight();//检测匹配战场结束时间
            battle.checkEliteFightTimeout();
        }
    }

    public void syncBattleFightUpdateInfo() {
        for (FamilyWarKnockoutBattle battle : battleMap.values()) {
            try {
                battle.sendBattleFightUpdateInfo();
            } catch (Exception e) {
                LogUtil.error("", e);
            }
        }
    }

    public void match() {
        for (FamilyWarKnockoutBattle battle : battleTypeOfBattleMap.get(this.battleType).values()) {
            try {
                battle.match(FamilyWarConst.W_TYPE_REMOTE);
            } catch (Exception e) {
                LogUtil.info("familywar|匹配出现异常 battleId:{}", battle.getBattleId());
                e.printStackTrace();
            }
        }
        if (this.battleType == R_BATTLE_TYPE_FINAL) {
            for (FamilyWarKnockoutBattle battle : battleTypeOfBattleMap.get(R_BATTLE_TYPE_3RD4TH).values()) {
                try {
                    battle.match(FamilyWarConst.W_TYPE_REMOTE);
                } catch (Exception e) {
                    LogUtil.info("familywar|匹配出现异常 battleId:{}", battle.getBattleId());
                    e.printStackTrace();
                }
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

    }

    @Override
    public Map<Long, KnockoutFamilyInfo> getFamilyMap() {
        return familyMap;
    }

    @Override
    public void updateElitePoints(String fighterUid, long delta) {
        updatePoints(elitePointsMap, elitePointsRankList, fighterUid, delta);
    }

    @Override
    public void updateNormalPoints(String fighterUid, long delta) {
        updatePoints(normalPointsMap, normalPointsRankList, fighterUid, delta);
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
                        FamilyWarUtil.getNearBattleEndTimeL(ActConst.ID_FAMILY_WAR_REMOTE) - System.currentTimeMillis()));
    }

    @Override
    public void enter(int controlServerId, int mainServerId, long familyId, long roleId, FighterEntity fighterEntity) {
        int thisStep = FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW;
        LogUtil.info("familywar|家族:{} 的玩家:{} 请求进入战场|当前步数:{}|获得最近一场比赛的开始时间:{}", familyId, roleId, thisStep, FamilyWarUtil.getNearBattleTimeStr(ActConst.ID_FAMILY_WAR_REMOTE));
        if (thisStep == FamilyWarRemoteFlow.STEP_BEFORE_32TO16 ||
                thisStep == FamilyWarRemoteFlow.STEP_BEFORE_16TO8 ||
                thisStep == FamilyWarRemoteFlow.STEP_BEFORE_8TO4 ||
                thisStep == FamilyWarRemoteFlow.STEP_BEFORE_4TO2 ||
                thisStep == FamilyWarRemoteFlow.STEP_BEFORE_FINNAL) {
            long remainTime = (FamilyWarUtil.getNearBattleTimeL(ActConst.ID_FAMILY_WAR_REMOTE) - System.currentTimeMillis()) / 1000;
            if (remainTime <= 0) {
                remainTime = 1;
            }
            roleService().warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_nextround"), remainTime));
            return;
        }
        StringBuilder stringBuilder = TimeUtil.getChinaShow(FamilyWarUtil.getNearBattleTimeL(ActConst.ID_FAMILY_WAR_REMOTE));
        if (thisStep == FamilyWarRemoteFlow.STEP_END_8TO4 ||
                thisStep == FamilyWarRemoteFlow.STEP_END_4TO2 ||
                thisStep == FamilyWarRemoteFlow.STEP_END_16TO8 ||
                thisStep == FamilyWarRemoteFlow.STEP_END_32TO16 ||
                thisStep == FamilyWarRemoteFlow.STEP_END_FINNAL ||
                thisStep == FamilyWarRemoteFlow.STEP_END_REMOTE) {
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
            roleService().warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_begintext"), FamilyWarUtil.getNearBattleTimeStr(ActConst.ID_FAMILY_WAR_REMOTE)));
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
            StringBuilder stringBuilder = TimeUtil.getChinaShow(FamilyWarUtil.getNearBattleTimeL(ActConst.ID_FAMILY_WAR_REMOTE));
            roleService().warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_nexturn"), stringBuilder.toString()));
        }
    }

    @Override
    public void enterNormalFightWaitingQueue(int controlServerId, int mainServerId, long familyId, long roleId, FighterEntity fighterEntity) {
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
            roleService().warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_begintext"), FamilyWarUtil.getNearBattleTimeStr(ActConst.ID_FAMILY_WAR_REMOTE)));
            return;
        }
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle != null) {
            familyInfo.getMemberMap().get(roleId).setFighterEntity(fighterEntity);
            battle.enterNormalFightWaitingQueue(controlServerId, mainServerId, familyId, roleId, fighterEntity);
        } else {
            if (familyInfo.isWinner()) {
                roleService().warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_begintext"), FamilyWarUtil.getNearBattleTimeStr(ActConst.ID_FAMILY_WAR_REMOTE)));
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

    @Override
    public void handleFighterQuit(String battleId, long roleId, String fightId) {
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle != null) {
            battle.handleFighterQuit(roleId, fightId);
        }
    }

    @Override
    public IndexList getElitePointsRankList(long roleId) {
        return elitePointsRankList;
    }

    @Override
    public IndexList getNormalPointsRankList(long roleId) {
        return normalPointsRankList;
    }

    public void sendAward_ResetPoints_NoticeMater(int step, long countdown) {
        sendAward(step);
        resetPoint();
        noticeMater(step, countdown);
        LogUtil.info("familywar|发奖，重置积分，通知族长 battleType:{},step", battleType, step);
        if (battleType == R_BATTLE_TYPE_OVER) {
            ServiceHelper.familyWarRemoteService().SyncBattleType(MultiServerHelper.getServerId(), battleType);
            ServiceHelper.familyWarRemoteService().updateFlowInfo(MultiServerHelper.getServerId(), FamilyWarConst.W_TYPE_REMOTE, FamilyWarConst.DISAPPEAR_APPLY_BUTTON);
            ServiceHelper.familywarRankService().resetTitle(MultiServerHelper.getServerId(), true);
        }
    }

    private void noticeMater(int step, long countdown) {
        if (step != FamilyWarRemoteFlow.STEP_END_REMOTE && battleType != R_BATTLE_TYPE_OVER) {
            sendMainIconToMaster(FamilyWarConst.W_TYPE_REMOTE);
        }
    }

    private void resetPoint() {
        elitePointsRankList = new IndexList(5000, 100, -5000);
        normalPointsRankList = new IndexList(5000, 100, -5000);
        elitePointsMap = new HashMap<>();
        normalPointsMap = new HashMap<>();
        eliteMinPointsAwardAcquiredRecordSet = new ConcurrentHashMap<>();
        normalMinPointsAwardAcquiredRecordSet = new ConcurrentHashMap<>();
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
        int rank = 1;
        for (RankObj obj : elitePointsRankList.getAll()) {
            FamilyWarPointsRankObj rankObj = (FamilyWarPointsRankObj) obj;
            Map<Long, Integer> submap = map.get(rankObj.getServerId());
            if (submap == null) {
                map.put(rankObj.getServerId(), submap = new HashMap<Long, Integer>());
            }
            submap.put(Long.parseLong(rankObj.getKey()), rank++);
        }
        // 调用localservice进行发奖
        for (Map.Entry<Integer, Map<Long, Integer>> entry : map.entrySet()) {
            ServiceHelper.familyWarRemoteService().sendPointsRankAward(FamilyWarUtil.getFamilyWarServerId(), entry.getKey(), true, entry.getValue());
        }

        // 发匹配的
        // 先分组
        map = new HashMap<>();
        rank = 1;
        for (RankObj obj : normalPointsRankList.getAll()) {
            FamilyWarPointsRankObj rankObj = (FamilyWarPointsRankObj) obj;
            Map<Long, Integer> submap = map.get(rankObj.getServerId());
            if (submap == null) {
                map.put(rankObj.getServerId(), submap = new HashMap<Long, Integer>());
            }
            submap.put(Long.parseLong(rankObj.getKey()), rank++);
        }
        // 调用localservice进行发奖
        for (Map.Entry<Integer, Map<Long, Integer>> entry : map.entrySet()) {
            ServiceHelper.familyWarRemoteService().sendPointsRankAward(FamilyWarUtil.getFamilyWarServerId(), entry.getKey(), false, entry.getValue());
        }
    }

    private void sendFamilyRankAwardAndUnlock(int step) {
        LogUtil.info("familywar|发奖与解锁家族 发奖状态:{},当前状态:{},被淘汰的家族:{}", sendAwardBattleType, battleType, outOfWarFamily.get(sendAwardBattleType));
        Set<Long> familyIds = outOfWarFamily.get(sendAwardBattleType);
        if (sendAwardBattleType != R_BATTLE_TYPE_FINAL && sendAwardBattleType != R_BATTLE_TYPE_4TO2) {
            if (familyIds != null) {
                for (long familyId : familyIds) {
                    sendFamilyRankAward(W_TYPE_REMOTE, familyId, sendAwardBattleType);
                }
            }
            for (KnockoutFamilyInfo familyInfo : familyMap.values()) {
                if (failFamilySet.contains(familyInfo.getFamilyId())) continue;
                FamilyWarRpcHelper.familyWarService().halfLockFamily(familyInfo.getMainServerId(), familyInfo.getFamilyId());
            }
        } else if (sendAwardBattleType == R_BATTLE_TYPE_FINAL) {
            LogUtil.info("familywar|前四名发奖:{} ,1st:{},2nd:{},3rd:{},4th:{}", familyIds, _1stFamilyId, _2ndFamilyId, _3rdFamilyId, _4thFamilyId);
            if (familyIds != null) {
                for (long familyId : familyIds) {
                    if (familyId == _1stFamilyId) {
                        sendFamilyRankAward(W_TYPE_REMOTE, familyId, 1);
                    } else if (familyId == _2ndFamilyId) {
                        sendFamilyRankAward(W_TYPE_REMOTE, familyId, 2);
                    }
                }
            }
            Set<Long> _34FamilyIds = outOfWarFamily.get(R_BATTLE_TYPE_3RD4TH);
            if (_34FamilyIds != null) {
                for (long familyId : _34FamilyIds) {
                    if (familyId == _3rdFamilyId) {
                        sendFamilyRankAward(W_TYPE_REMOTE, familyId, 3);
                    } else if (familyId == _4thFamilyId) {
                        sendFamilyRankAward(W_TYPE_REMOTE, familyId, 4);
                    }
                }
            }
        }
        Map<Integer, List<Long>> serverFamilyMap = new HashMap<>();
        if (familyIds != null) {
            for (long familyId : familyIds) {
                KnockoutFamilyInfo info = familyMap.get(familyId);
                List<Long> familyIdList = serverFamilyMap.get(info.getMainServerId());
                if (familyIdList == null) {
                    familyIdList = new ArrayList<>();
                    serverFamilyMap.put(info.getMainServerId(), familyIdList);
                }
                familyIdList.add(familyId);
            }
        }
        for (Map.Entry<Integer, List<Long>> entry : serverFamilyMap.entrySet()) {
            FamilyWarRpcHelper.familyWarService().unLockFamily(entry.getKey(), entry.getValue());
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

    public void onStageFightCreateSucceeded(int mainServerId, int fightServerId, String battleId, String fightId, long roleId) {
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle == null) return;
        battle.handleFighterEnter(roleId, fightId);
        roleService().send(mainServerId, roleId, new ClientFamilyWarBattleFightPersonalPoint(battle.getBattleNormalPersonalPosints(Long.toString(roleId))));
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

    public final void onClientPreloadFinished(int mainServerId, String battleId, String fightId, long roleId) {
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle != null) {
            battle.onClientPreloadFinished(mainServerId, fightId, roleId);
        } else {
            LogUtil.info("familywar|onClientPreloadFinished: no such battle {}", battleId);
        }
    }

    public void onEliteFighterAddingSucceeded(int mainServerId, int fightServerId, String battleId, String fightId, long roleId) {
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle == null) return;
        battle.handleFighterEnter(roleId, fightId);
    }

    public void AsyncFihterEntityAndLockFamily() {
        Map<Integer, Set<Long>> roleServerMap = new HashMap<>();
        for (KnockoutFamilyInfo info : familyMap.values()) {
            if (failFamilySet.contains(info.getFamilyId())) continue;
            Set<Long> roleSet = roleServerMap.get(info.getMainServerId());
            if (roleSet == null) {
                roleSet = new HashSet<>();
                roleServerMap.put(info.getMainServerId(), roleSet);
            }
            roleSet.addAll(info.getMemberMap().keySet());
            FamilyWarRpcHelper.familyWarService().lockFamily(info.getMainServerId(), info.getFamilyId());
        }
        for (Map.Entry<Integer, Set<Long>> entry : roleServerMap.entrySet()) {
            FamilyWarRpcHelper.familyWarService().updateFighterEntity(entry.getKey(), FamilyWarConst.W_TYPE_REMOTE, entry.getValue());
        }
    }

    public void updateFighterEntity(Map<Long, FighterEntity> entityMap) {
        for (Map.Entry<Long, FighterEntity> entry : entityMap.entrySet()) {
            try {
                familyMap.get(memberMap.get(entry.getKey()).getFamilyId()).getMemberMap().get(entry.getKey()).setFighterEntity(entry.getValue());
                roleService().notice(getMainServerId(entry.getKey()), entry.getKey(), new FamilyWarFightingOrNotEvent(true));
            } catch (Exception e) {
                LogUtil.info("familywar|同步玩家数据出现异常 roleId:{}", entry.getKey());
                e.printStackTrace();
            }
        }
        LogUtil.info("familywar|跨服海选 玩家 {} 战斗实体同步完毕", entityMap.keySet());
    }

    public void updateIconText(String text, Map<Integer, Boolean> allCanRemoteServerMap) {
        for (int id : allCanRemoteServerMap.keySet()) {
            if ((FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW == FamilyWarRemoteFlow.STEP_END_8TO4 && battleType == R_BATTLE_TYPE_OVER)
                    || FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW == FamilyWarRemoteFlow.STEP_END_4TO2 && battleType == R_BATTLE_TYPE_OVER
                    || FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW == FamilyWarRemoteFlow.STEP_END_FINNAL) {
                String time = FamilyWarUtil.getBattleTimeStr(FamilyWarRemoteFlow.STEP_END_REMOTE, ActConst.ID_FAMILY_WAR_REMOTE);
                String tmpStr = String.format(DataManager.getGametext("familywar_desc_awardtime"), time);
                FamilyWarRpcHelper.familyWarService().setOptions(id, ActConst.ID_FAMILY_WAR_GENERAL,
                        FamilyConst.ACT_BTN_MASK_DISPLAY | FamilyConst.ACT_BTN_MASK_LIGHT, -1, tmpStr);
                continue;
            }
            if ((FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW == FamilyWarRemoteFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_4TO2 && battleType == R_BATTLE_TYPE_OVER)
                    || FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW == FamilyWarRemoteFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_FINNAL && battleType == R_BATTLE_TYPE_OVER
                    || FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW == FamilyWarRemoteFlow.STEP_END_REMOTE) {
                FamilyWarRpcHelper.familyWarService().setOptions(id, ActConst.ID_FAMILY_WAR_GENERAL,
                        FamilyConst.ACT_BTN_MASK_DISPLAY | FamilyConst.ACT_BTN_MASK_LIGHT, -1, "已结束");
                continue;
            }
            if (!text.equals("")) {
                FamilyWarRpcHelper.familyWarService().setOptions(id, ActConst.ID_FAMILY_WAR_GENERAL,
                        FamilyConst.ACT_BTN_MASK_DISPLAY | FamilyConst.ACT_BTN_MASK_LIGHT, -1, text);
            } else {
                long time = FamilyWarUtil.getNearBattleTimeL(ActConst.ID_FAMILY_WAR_REMOTE);
                StringBuilder tmpStr0 = TimeUtil.getChinaShow(time);
                String tmpStr = String.format(DataManager.getGametext("familywar_desc_fightbegintime"), tmpStr0.toString());
                FamilyWarRpcHelper.familyWarService().setOptions(id, ActConst.ID_FAMILY_WAR_GENERAL,
                        FamilyConst.ACT_BTN_MASK_DISPLAY | FamilyConst.ACT_BTN_MASK_LIGHT, -1, tmpStr);
                LogUtil.info("familywar|serverId:{},timStr:{}", id, tmpStr);
            }
        }
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

    public Map<String, FamilyWarKnockoutBattle> getBattleMap() {
        return battleMap;
    }

    public Map<Long, KnockoutFamilyMemberInfo> getMemberMap() {
        return memberMap;
    }

    public void initRoleTips(KnockoutFamilyInfo info) {
        for (long roleId : info.getMemberMap().keySet()) {
            remoteTipMap.put(roleId, true);
        }
    }

    public void setIsDisaster(int isDisaster) {
        this.isDisaster = isDisaster;
    }

    public void setDisasterFixture(long[] disasterFixture) {
        this.disasterFixture = disasterFixture;
    }
}
