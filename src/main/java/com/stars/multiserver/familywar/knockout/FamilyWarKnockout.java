package com.stars.multiserver.familywar.knockout;

import com.stars.core.dao.DbRowDao;
import com.stars.modules.MConst;
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
import com.stars.modules.familyactivities.war.prodata.FamilyWarRankAwardVo;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.serverLog.EventType;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.*;
import com.stars.multiserver.familywar.event.FamilyWarSupportEvent;
import com.stars.multiserver.familywar.flow.FamilyWarFlow;
import com.stars.multiserver.familywar.flow.FamilyWarKnockoutFlow;
import com.stars.multiserver.familywar.knockout.fight.elite.FamilyWarEliteFightStat;
import com.stars.multiserver.fight.FightIdCreator;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.services.activities.ActConst;
import com.stars.services.chat.ChatManager;
import com.stars.services.family.FamilyPost;
import com.stars.services.fightbase.FightBaseService;
import com.stars.services.localservice.LocalService;
import com.stars.services.role.RoleService;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryComponent;
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
import static com.stars.multiserver.familywar.flow.FamilyWarKnockoutFlow.STEP_END_KNOCKOUT;

/**
 * 流程控制
 * 1. 提取名单（通过排行榜获取），锁定家族，准备参赛名单
 * 2. 锁定参赛名单（例如，提前5分钟不能修改）
 * 3. 开始四分之一决赛
 * 4. 检测并强制停止未完成的四分之一决赛
 * 5. 发奖?
 * 6. 开始二分之决赛
 * 7. 检测并强制停止未完成的二分之一决赛
 * 8. 开始决赛和三四名决赛
 * 9. 检测并强制停止未完成的赛事
 * 10.发奖（，提交数据）
 * <p>
 * 发奖
 * 1. 个人积分
 * 2. 家族奖励（族长，参战成员，家族成员）
 * 3. 积分达标奖励
 * Created by zhaowenshuo on 2016/11/24.
 */
public class FamilyWarKnockout extends FamilyWar {

    /* 数据工具类 */
    private DbRowDao dao;

    /* 外围服务，外部注入，用于屏蔽本服/跨服家族战差异 */
    private FightBaseService fightService;
    private RoleService roleService;
    private FamilyWarLocalService familyWarLocalService;
    private LocalService localService;

    /* 淘汰赛相关信息（整个淘汰赛） */
//    private Map<Long, KnockoutFamilyInfo> familyMap; // 家族信息表，(familyId, KnockoutFamilyInfo)
//    private Set<Long> failFamilySet;//未能晋级的家族信息表
//    private Map<Long, KnockoutFamilyMemberInfo> memberMap; // 家族成员信息表，(memberId, KnockoutFamilyMemberInfo)
    private long[] fixtures; // 赛程

    /* 战斗表 */
    private Map<String, FamilyWarKnockoutBattle> battleMap; // 战斗表
    private Map<String, FamilyWarKnockoutBattle> quarterFinalBattleMap; // 四分之一决赛
    private Map<String, FamilyWarKnockoutBattle> semiFinalBattleMap; // 二分之一决赛
    private Map<String, FamilyWarKnockoutBattle> finalBattleMap; // 决赛

    /* 名次 */
    private long _1stFamilyId = 0L;
    private long _2ndFamilyId = 0L;
    private long _3rdFamilyId = 0L;
    private long _4thFamilyId = 0L;

//    /* 个人积分相关 */
//    private IndexList elitePointsRankList; // 精英战个人积分榜（前几名）
//    private IndexList normalPointsRankList; // 匹配战个人积分榜（前几名）
//    private Map<String, Long> elitePointsMap; // 个人积分（所有的）
//    private Map<String, Long> normalPointsMap; // 匹配战积分（所有的）
//
//    /* 个人积分达标奖励相关 */
//    private Map<Long, Set<Long>> eliteMinPointsAwardAcquiredRecordSet; // 已领取的积分奖励
//    private Map<Long, Set<Long>> normalMinPointsAwardAcquiredRecordSet; // 已领取的积分奖励
//
//    private Map<Long, Boolean> hasNoticeMasterMap;//masterId,true or false

    public FamilyWarKnockout() {
//        elitePointsRankList = new IndexList(5000, 100, -5000);
//        normalPointsRankList = new IndexList(5000, 100, -5000);
//        elitePointsMap = new HashMap<>();
//        normalPointsMap = new HashMap<>();
//        familyMap = new HashMap<>();
//        memberMap = new HashMap<>();
//        eliteMinPointsAwardAcquiredRecordSet = new ConcurrentHashMap<>();
//        normalMinPointsAwardAcquiredRecordSet = new ConcurrentHashMap<>();
//        failFamilySet = new HashSet<>();
//        hasNoticeMasterMap = new HashMap<>();
        super();
        fixtures = new long[0];
        battleMap = new HashMap<>();
        quarterFinalBattleMap = new HashMap<>();
        semiFinalBattleMap = new HashMap<>();
        finalBattleMap = new HashMap<>();
    }

    public void printState() {
        LogUtil.info("容器大小输出:{},battleMap:{},quarterFinalBattleMap:{},semiFinalBattleMap:{},finalBattleMap:{}", this.getClass().getSimpleName(), battleMap.size(), quarterFinalBattleMap.size(), semiFinalBattleMap.size(), finalBattleMap.size());
        for (FamilyWarKnockoutBattle battle : battleMap.values()) {
            battle.printState();
        }
    }

    public void sendAward_ResetPoints_NoticeMater(int step, long countdown) {
        sendAward(step);
        resetPoint();
        noticeMater(step, countdown);
    }

    private void sendAward(int step) {
        LogUtil.info("familywar|发奖阶段|step:{},1st:{},2nd:{},3rd:{},4th:{}|", step, _1stFamilyId, _2ndFamilyId, _3rdFamilyId, _4thFamilyId);
        sendRolePointsRank();
        sendFamilyRankAwardAndUnlock(step);
    }

    private void resetPoint() {
        elitePointsRankList = new IndexList(5000, 100, -5000);
        normalPointsRankList = new IndexList(5000, 100, -5000);
        elitePointsMap = new HashMap<>();
        normalPointsMap = new HashMap<>();
        eliteMinPointsAwardAcquiredRecordSet = new ConcurrentHashMap<>();
        normalMinPointsAwardAcquiredRecordSet = new ConcurrentHashMap<>();
    }

    private void noticeMater(int step, long countdown) {
        if (step != STEP_END_KNOCKOUT) {
            sendMainIconToMaster(FamilyWarConst.W_TYPE_LOCAL);
        }
    }

    // fixme: 入库
    public void viewMinPointsAward(int mainServerId, long roleId) {
        byte minAwardType = MIN_AWARD_ELITE;
        Long points;
        Set<Long> recordSet = null;
        if (elitePointsMap.containsKey(Long.toString(roleId))) {
            minAwardType = MIN_AWARD_ELITE;
            points = elitePointsMap.get(Long.toString(roleId));
            recordSet = eliteMinPointsAwardAcquiredRecordSet.get(roleId);
        } else if (normalPointsMap.containsKey(Long.toString(roleId))) {
            minAwardType = MIN_AWARD_NORMAL;
            points = normalPointsMap.get(Long.toString(roleId));
            recordSet = normalMinPointsAwardAcquiredRecordSet.get(roleId);
        } else {
            points = null;
        }
        ClientFamilyWarUiMinPointsAward packet = new ClientFamilyWarUiMinPointsAward(
                ClientFamilyWarUiMinPointsAward.SUBTYPE_VIEW, minAwardType, points == null ? 0 : points.longValue(), recordSet);
        roleService().send(mainServerId, roleId, packet);
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
            familyWarLocalService().sendPointsRankAward(entry.getKey(), true, true, entry.getValue());
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
            familyWarLocalService().sendPointsRankAward(entry.getKey(), true, false, entry.getValue());
        }
    }

    public void sendFamilyRankAwardAndUnlock(int step) {
        if (step != STEP_END_KNOCKOUT) {
            for (long familyId : familyMap.keySet()) {
                try {
                    LogUtil.info("familywar|家族 {} 进入半锁状态", familyId);
                    ServiceHelper.familyMainService().halfLockFamily(familyId);
                } catch (Exception e) {
                    LogUtil.info("familywar|半锁家族:{}出现异常|{}", familyId, e);
                }
            }
        } else {
            for (Long familyId : familyMap.keySet()) {
                if (familyId == _1stFamilyId) {
                    sendFamilyRankAward(W_TYPE_LOCAL, familyId, 1); // 第一名
                } else if (familyId == _2ndFamilyId) {
                    sendFamilyRankAward(W_TYPE_LOCAL, familyId, 2); // 第二名
                } else if (familyId == _3rdFamilyId) {
                    sendFamilyRankAward(W_TYPE_LOCAL, familyId, 3); // 第三名
                } else if (familyId == _4thFamilyId) {
                    sendFamilyRankAward(W_TYPE_LOCAL, familyId, 4); // 第四名
                } else {
                    sendFamilyRankAward(W_TYPE_LOCAL, familyId, 5); // 第五到八名
                }
            }
            //解锁家族
            for (long familyId : familyMap.keySet()) {
                try {
                    ServiceHelper.familyMainService().unlockFamily(familyId);
                } catch (Exception e) {
                    LogUtil.info("familywar|解锁家族:{}出现异常|{}", familyId, e);
                }
            }
        }
    }

//    public void addFamilyInfo(KnockoutFamilyInfo familyInfo) {
//        familyMap.put(familyInfo.getFamilyId(), familyInfo);
//        memberMap.putAll(familyInfo.getMemberMap());
//        hasNoticeMasterMap.put(familyInfo.getMasterId(), false);
//    }

    /**
     * 是否参赛家族
     *
     * @param familyId
     * @return
     */
    public boolean isKnockoutFamily(long familyId) {
        return familyMap.containsKey(familyId) && !failFamilySet.contains(familyId);
    }

    private void addFailFamily(long familyId) {
        failFamilySet.add(familyId);
    }

    public void generateFixtures(Map<Integer, Long> fixtureFamilyMap) {
        LogUtil.info("familywar|fixturesFamily:{}", fixtureFamilyMap);
        fixtures = new long[16];
        for (Map.Entry<Integer, Long> entry : fixtureFamilyMap.entrySet()) {
            fixtures[entry.getKey()] = entry.getValue();
        }
        // 设置家族的对阵序列
        for (int i = 0; i < fixtures.length; i++) {
            long familyId = fixtures[i];
            if (familyId == 0L) continue;
            KnockoutFamilyInfo info = familyMap.get(familyId);
            if (info == null) continue;
            if (info.getSeq() == 0 || info.getSeq() > i) {
                info.setSeq(i);
            }
        }
        FamilyWarConst.STEP_OF_GENERAL_FLOW = FamilyWarFlow.STEP_LOCAL_KNOCKOUT_START;
        int preStep = FamilyWarUtil.getPreStep(ActConst.ID_FAMILY_WAR_LOCAL);
        LogUtil.info("familywar|fixtures:{},preStep:{}", fixtures, preStep);
        if (preStep == -1)
            throw new IllegalArgumentException("拿不到步数");
        FamilyWarConst.STEP_OF_SUB_FLOW = preStep;
    }

    /**
     * 生成四分之一决赛对阵表（也即是最后的对阵表）
     */
    public void generateFixtures() {
        fixtures = new long[16];
        // 战力排序
        List<KnockoutFamilyInfo> list = new ArrayList<>(familyMap.values());
        Collections.sort(list);//按照排行榜上的排名先做一次排序
        // 确定前四名的位置
        if (list.size() > 0) {
            fixtures[K_SEQ_QUARTER_A] = list.remove(0).getFamilyId();
        }
        if (list.size() > 0) {
            fixtures[K_SEQ_QUARTER_E] = list.remove(0).getFamilyId();
        }
        if (list.size() > 0) {
            fixtures[K_SEQ_QUARTER_C] = list.remove(0).getFamilyId();
        }
        if (list.size() > 0) {
            fixtures[K_SEQ_QUARTER_G] = list.remove(0).getFamilyId();
        }
        // 生成剩余的位置
        List<Integer> tempList = new ArrayList<>();
        tempList.add(K_SEQ_QUARTER_B);
        tempList.add(K_SEQ_QUARTER_D);
        tempList.add(K_SEQ_QUARTER_F);
        tempList.add(K_SEQ_QUARTER_H);
        Random random = new Random();
        int size = tempList.size();
        for (int i = 0; i < size && list.size() > 0; i++) {
            long familyId = list.remove(0).getFamilyId();
            int idx = random.nextInt(tempList.size());
            int seq = tempList.remove(idx);
            fixtures[seq] = familyId;
        }
        // 设置家族的对阵序列
        for (int seq = K_SEQ_QUARTER_A; seq <= K_SEQ_QUARTER_H; seq++) {
            long familyId = fixtures[seq];
            KnockoutFamilyInfo info = familyMap.get(familyId);
            if (info == null) continue;
            info.setSeq(seq);
        }
        updateFamilyFixture();
    }

    private void updateFamilyFixture() {
        Map<Integer, Long> fixtureFamilyMap = new HashMap<>();
        for (int i = 0; i < fixtures.length; i++) {
            fixtureFamilyMap.put(i, fixtures[i]);
        }
        ServiceHelper.familyWarService().updateFamilyWarFixture(MultiServerHelper.getServerId(), FamilyWarConst.W_TYPE_LOCAL, fixtureFamilyMap);
    }

    /**
     * 开启普通赛匹配线程
     */
    public void startMatch() {
        if (FamilyActWarManager.matchScheduler == null) {
            FamilyActWarManager.matchScheduler = Executors.newSingleThreadScheduledExecutor();
            FamilyActWarManager.matchScheduler.scheduleAtFixedRate(FamilyActWarManager.matchTask,
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

    public void AsyncFihterEntity() {
        Set<Long> roleIds = new HashSet<>();
        for (KnockoutFamilyInfo familyInfo : familyMap.values()) {
            roleIds.addAll(familyInfo.getMemberMap().keySet());
            ServiceHelper.familyMainService().lockFamily(familyInfo.getFamilyId());
        }
        for (long roleId : roleIds) {
            try {
                Summary summary = ServiceHelper.summaryService().getSummary(roleId);
                FighterEntity fighterEntity = FighterCreator.createBySummary((byte) 1, summary).get(Long.toString(roleId));
                memberMap.get(roleId).setFighterEntity(fighterEntity);
                roleService().notice(roleId, new FamilyWarFightingOrNotEvent(true));
                LogUtil.info("familywar|本服淘汰赛  玩家 {} 战斗实体同步完毕", roleId);
            } catch (Exception e) {
                LogUtil.info("familywar|本服淘汰赛  拿取 {} 玩家摘要数据出现异常|e", roleId, e);
                e.printStackTrace();
            }
        }
    }

    /**
     * 开始四分之一决赛
     * 1. 根据对阵表生成battle
     */
    public void startQuarterFinals() {
        // battleMap is null?
        for (int i = K_SEQ_SEMI_I; i <= K_SEQ_SEMI_L; i++) {
            long camp1FamilyId = fixtures[(i + 1) * 2];
            long camp2FamilyId = fixtures[(i + 1) * 2 + 1];

            //轮空处理
            if (camp1FamilyId == 0 && camp2FamilyId == 0) {
                continue;
            } else if (camp1FamilyId == 0 && camp2FamilyId != 0) {
                handleEmptyBattle(FamilyWarConst.K_BATTLE_TYPE_QUARTER, camp2FamilyId, camp1FamilyId);
                continue;
            } else if (camp1FamilyId != 0 && camp2FamilyId == 0) {
                handleEmptyBattle(FamilyWarConst.K_BATTLE_TYPE_QUARTER, camp1FamilyId, camp2FamilyId);
                continue;
            }

            // todo: generate a battle id
            String battleId = "fwquarter-" + MultiServerHelper.getServerId() + "-" + getSeqMark((i + 1) * 2) + getSeqMark((i + 1) * 2 + 1) + "-" + FightIdCreator.creatUUId();
            FamilyWarKnockoutBattle battle =
                    new FamilyWarKnockoutBattle(battleId, FamilyWarConst.K_BATTLE_TYPE_QUARTER, familyMap.get(camp1FamilyId), familyMap.get(camp2FamilyId));
            battle.setFamilyWar(this);
            battleMap.put(battleId, battle);
            quarterFinalBattleMap.put(battleId, battle);
            familyMap.get(camp1FamilyId).setBattleId(battleId);
            familyMap.get(camp2FamilyId).setBattleId(battleId);
//            hasNoticeMasterMap.put(familyMap.get(camp1FamilyId).getMasterId(), false);
//            hasNoticeMasterMap.put(familyMap.get(camp2FamilyId).getMasterId(), false);
            // log
        }
        // start
        for (FamilyWarKnockoutBattle battle : quarterFinalBattleMap.values()) {
            battle.start(FamilyWarConst.W_TYPE_LOCAL);
        }
        startMatch();
    }

    public void endQuarterFinals() {
        endMatch();
        for (FamilyWarKnockoutBattle battle : quarterFinalBattleMap.values()) {
//            battle.end(false);
            battle.finishAllFight();
        }
        // todo: do some job
        for (FamilyWarKnockoutBattle battle : quarterFinalBattleMap.values()) {
            battleMap.remove(battle.getBattleId());
        }
        quarterFinalBattleMap.clear();
        List<Long> roleIds = new ArrayList<>();
        for (KnockoutFamilyInfo familyInfo : familyMap.values()) {
            roleIds.addAll(familyInfo.getMemberMap().keySet());
            if (failFamilySet.contains(familyInfo.getFamilyId()))
                continue;
            hasNoticeMasterMap.put(familyInfo.getMasterId(), false);
        }
        unLockRoleState(roleIds);
    }

    protected void unLockRoleState(List<Long> roleIds) {
        for (long roleId : roleIds) {
            roleService().notice(roleId, new FamilyWarFightingOrNotEvent(false));
        }
    }

    /**
     * 开始二分之一决赛
     */
    public void startSemiFinals() {
        // battleMap is null?
        for (int i = K_SEQ_FINAL_M; i <= K_SEQ_FINAL_N; i++) {
            long camp1FamilyId = fixtures[(i + 1) * 2];
            long camp2FamilyId = fixtures[(i + 1) * 2 + 1];

            //轮空处理
            if (camp1FamilyId == 0 && camp2FamilyId == 0) {
                continue;
            } else if (camp1FamilyId == 0 && camp2FamilyId != 0) {
                handleEmptyBattle(FamilyWarConst.K_BATTLE_TYPE_SEMI, camp2FamilyId, camp1FamilyId);
                continue;
            } else if (camp1FamilyId != 0 && camp2FamilyId == 0) {
                handleEmptyBattle(FamilyWarConst.K_BATTLE_TYPE_SEMI, camp1FamilyId, camp2FamilyId);
                continue;
            }

            // todo: generate a battle id
            String battleId = "fwsemi-" + MultiServerHelper.getServerId() + "-" + getSeqMark((i + 1) * 2) + getSeqMark((i + 1) * 2 + 1) + "-" + FightIdCreator.creatUUId();
            FamilyWarKnockoutBattle battle =
                    new FamilyWarKnockoutBattle(battleId, FamilyWarConst.K_BATTLE_TYPE_SEMI, familyMap.get(camp1FamilyId), familyMap.get(camp2FamilyId));
            battle.setFamilyWar(this);
            battleMap.put(battleId, battle);
            semiFinalBattleMap.put(battleId, battle);
            familyMap.get(camp1FamilyId).setBattleId(battleId);
            familyMap.get(camp2FamilyId).setBattleId(battleId);
//            hasNoticeMasterMap.put(familyMap.get(camp1FamilyId).getMasterId(), false);
//            hasNoticeMasterMap.put(familyMap.get(camp2FamilyId).getMasterId(), false);
            // log
        }
        // start
        for (FamilyWarKnockoutBattle battle : semiFinalBattleMap.values()) {
            battle.start(FamilyWarConst.W_TYPE_LOCAL);
        }
        startMatch();
    }

    public void endSemiFinals() {
        endMatch();
        for (FamilyWarKnockoutBattle battle : semiFinalBattleMap.values()) {
//            battle.end(false);
            battle.finishAllFight();
        }
        // todo: do some job
        for (FamilyWarKnockoutBattle battle : semiFinalBattleMap.values()) {
            battleMap.remove(battle.getBattleId());
        }
        semiFinalBattleMap.clear();
        List<Long> roleIds = new ArrayList<>();
        for (KnockoutFamilyInfo familyInfo : familyMap.values()) {
            roleIds.addAll(familyInfo.getTeamSheet());
            if (failFamilySet.contains(familyInfo.getFamilyId()))
                continue;
            hasNoticeMasterMap.put(familyInfo.getMasterId(), false);
        }
        unLockRoleState(roleIds);
    }

    /**
     * 开始一二名决赛
     */
    public void startFinal() {
        // battleMap is null?
        // 决赛
        long camp1FamilyId = fixtures[K_SEQ_FINAL_M];
        long camp2FamilyId = fixtures[K_SEQ_FINAL_N];

        //轮空处理
        if (camp1FamilyId == 0 && camp2FamilyId == 0) {
            return;
        } else if (camp1FamilyId == 0 && camp2FamilyId != 0) {
            handleEmptyBattle(FamilyWarConst.K_BATTLE_TYPE_FINAL, camp2FamilyId, camp1FamilyId);
            return;
        } else if (camp1FamilyId != 0 && camp2FamilyId == 0) {
            handleEmptyBattle(FamilyWarConst.K_BATTLE_TYPE_FINAL, camp1FamilyId, camp2FamilyId);
            return;
        }

        // todo: generate a battle id
        String battleId = "fwfinal-MN-" + MultiServerHelper.getServerId() + "-" + FightIdCreator.creatUUId();
        FamilyWarKnockoutBattle battle =
                new FamilyWarKnockoutBattle(battleId, FamilyWarConst.K_BATTLE_TYPE_FINAL, familyMap.get(camp1FamilyId), familyMap.get(camp2FamilyId));
        battle.setFamilyWar(this);
        battleMap.put(battleId, battle);
        finalBattleMap.put(battleId, battle);
        familyMap.get(camp1FamilyId).setBattleId(battleId);
        familyMap.get(camp2FamilyId).setBattleId(battleId);
        // 3/4名决赛
        camp1FamilyId = fixtures[K_SEQ_FINAL_34_O];
        camp2FamilyId = fixtures[K_SEQ_FINAL_34_P];

        //轮空处理
        if (camp1FamilyId == 0 && camp2FamilyId == 0) {
            //没有3,4名
        } else if (camp1FamilyId == 0 && camp2FamilyId != 0) {
            handleEmptyBattle(FamilyWarConst.K_BATTLE_TYPE_FINAL_3RD4TH, camp2FamilyId, camp1FamilyId);
        } else if (camp1FamilyId != 0 && camp2FamilyId == 0) {
            handleEmptyBattle(FamilyWarConst.K_BATTLE_TYPE_FINAL_3RD4TH, camp1FamilyId, camp2FamilyId);
        } else {
            // todo: generate a battle id
            battleId = "fw34final-OP-" + "-" + MultiServerHelper.getServerId() + "-" + FightIdCreator.creatUUId();
            battle = new FamilyWarKnockoutBattle(battleId, FamilyWarConst.K_BATTLE_TYPE_FINAL_3RD4TH, familyMap.get(camp1FamilyId), familyMap.get(camp2FamilyId));
            battle.setFamilyWar(this);
            battleMap.put(battleId, battle);
            finalBattleMap.put(battleId, battle);
            familyMap.get(camp1FamilyId).setBattleId(battleId);
            familyMap.get(camp2FamilyId).setBattleId(battleId);
//            hasNoticeMasterMap.put(familyMap.get(camp1FamilyId).getMasterId(), false);
//            hasNoticeMasterMap.put(familyMap.get(camp2FamilyId).getMasterId(), false);
        }

        // start
        for (FamilyWarKnockoutBattle b : finalBattleMap.values()) {
            b.start(FamilyWarConst.W_TYPE_LOCAL);
        }
        startMatch();
    }

    public void endFinals() {
        //停止匹配
        endMatch();
        for (FamilyWarKnockoutBattle battle : finalBattleMap.values()) {
//            battle.end(false);
            battle.finishAllFight();
        }
        // todo: do some job
        for (FamilyWarKnockoutBattle battle : finalBattleMap.values()) {
            battleMap.remove(battle.getBattleId());
        }
        finalBattleMap.clear();
        List<Long> roleIds = new ArrayList<>();
        for (KnockoutFamilyInfo familyInfo : familyMap.values()) {
            roleIds.addAll(familyInfo.getTeamSheet());
        }
        unLockRoleState(roleIds);
        // 家族排名发奖
        // fixme: 确定周期
//        ServiceHelper.familyWarService().addFamilyWarData(FamilyWarService.LOCAL_KNOCKOUT_WINNERS, _1stFamilyId + "&" + _2ndFamilyId);
    }

    public void onFightCreatFail(String battleId, String fightId, int warType) {
        LogUtil.info("familywar|战斗创建失败|battleId:{},fightId:{},warType:{}", battleId, fightId, warType);
        FamilyWarKnockoutBattle battle = battleMap.remove(battleId);
        if (battle != null) {
            LogUtil.info("familywar|战斗创建失败|camp1FamilyId:{},camp2FamilyId:{},roleId2FamilyId:{}", battle.getCamp1FamilyId(), battle.getCamp2FamilyId(), battle.getRoleId2FamilyIdMap());
            if (warType == FamilyWarConst.WarTypeElite) {
                long camp1Id = battle.getCamp1FamilyId();
                long camp2Id = battle.getCamp2FamilyId();
                int battleType = battle.getType();
                String battleIds = "fwfailagain-" + "-" + MultiServerHelper.getServerId() + "-" + FightIdCreator.creatUUId();
                FamilyWarKnockoutBattle battleAgain =
                        new FamilyWarKnockoutBattle(battleIds, battleType, familyMap.get(camp1Id), familyMap.get(camp2Id));
                battleAgain.setFamilyWar(this);
                battleMap.put(battleIds, battleAgain);
                familyMap.get(camp1Id).setBattleId(battleIds);
                familyMap.get(camp2Id).setBattleId(battleIds);
                if (battleType == FamilyWarConst.K_BATTLE_TYPE_QUARTER) {
                    quarterFinalBattleMap.put(battleIds, battleAgain);
                    quarterFinalBattleMap.remove(battleId);
                } else if (battleType == FamilyWarConst.K_BATTLE_TYPE_SEMI) {
                    semiFinalBattleMap.put(battleIds, battleAgain);
                    semiFinalBattleMap.remove(battleId);
                } else {
                    finalBattleMap.put(battleIds, battleAgain);
                    finalBattleMap.remove(battleId);
                }
                battleAgain.start(FamilyWarConst.W_TYPE_LOCAL);
                LogUtil.info("familywar|战斗重新创建|battleId:{},warType:{}", battleIds, warType);
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
            familyWarLocalService().sendFamilyRankAward(
                    familyInfo.getMainServerId(), familyInfo.getFamilyName(), rank, rankAwardMap);
        } catch (Exception e) {
            LogUtil.info("familywar|发送家族:{}排名奖励出现异常|{}", familyId, e);
        }
    }

    /**
     * 更新积分
     *
     * @param fighterUid
     * @param delta
     */
    public void updateElitePoints(String fighterUid, long delta) {
        updatePoints(elitePointsMap, elitePointsRankList, fighterUid, delta);
    }

    public void updateNormalPoints(String fighterUid, long delta) {
        updatePoints(normalPointsMap, normalPointsRankList, fighterUid, delta);
    }

//    private void updatePoints(Map<String, Long> pointsMap, IndexList rankList, String fighterUid, long delta) {
//        Long points = pointsMap.get(fighterUid);
//        if (points == null) {
//            points = 0L;
//        }
//        points = points + delta;
//        pointsMap.put(fighterUid, points);
//        if (rankList.containsRank(fighterUid)) {
//            rankList.updateRank(fighterUid, points);
//        } else {
//            KnockoutFamilyMemberInfo memberInfo = memberMap.get(Long.parseLong(fighterUid));
//            if (memberInfo == null) return;
//            KnockoutFamilyInfo familyInfo = familyMap.get(memberInfo.getFamilyId());
//            if (familyInfo == null) return;
//            rankList.addRank(fighterUid, new FamilyWarPointsRankObj(
//                    fighterUid, points, memberInfo.getMainServerId(), memberInfo.getName(), familyInfo.getFamilyName()));
//        }
//    }

    public void enter(int controlServerId, int mainServerId, long familyId, long roleId, FighterEntity fighterEntity) {
        int thisStep = FamilyWarConst.STEP_OF_SUB_FLOW;
        LogUtil.info("familywar|家族:{} 的玩家:{} 请求进入战场|当前步数:{}|获得最近一场比赛的开始时间:{}", familyId, roleId, thisStep, FamilyWarUtil.getNearBattleTimeStr(ActConst.ID_FAMILY_WAR_LOCAL));
        if (thisStep == FamilyWarKnockoutFlow.STEP_BEFORE_QUARTER_FIANLS
                || thisStep == FamilyWarKnockoutFlow.STEP_BEFORE_SEMI_FIANLS
                || thisStep == FamilyWarKnockoutFlow.STEP_BEFORE_END_FIANLS) {
            long remainTime = (FamilyWarUtil.getNearBattleTimeL(ActConst.ID_FAMILY_WAR_LOCAL) - System.currentTimeMillis()) / 1000;
            if (remainTime <= 0) {
                remainTime = 1;
            }
            roleService.warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_nextround"), remainTime));
            return;
        }
        StringBuilder stringBuilder = TimeUtil.getChinaShow(FamilyWarUtil.getNearBattleTimeL(ActConst.ID_FAMILY_WAR_LOCAL));
        if (thisStep == FamilyWarKnockoutFlow.STEP_END_QUARTER_FINALS
                || thisStep == FamilyWarKnockoutFlow.STEP_END_SEMI_FINALS
                || thisStep == FamilyWarKnockoutFlow.STEP_END_FINALS) {
            roleService.warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_nexturn"), stringBuilder.toString()));
            return;
        }
        if (thisStep == STEP_END_KNOCKOUT) {
            roleService.warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_nexturn"), stringBuilder.toString()));
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

    public void enterSafeScene(int controlServerId, int mainServerId, long roleId) {
        KnockoutFamilyMemberInfo memberInfo = memberMap.get(roleId);
        if (memberInfo != null) {
            enterSafeScene(controlServerId, mainServerId, memberInfo.getFamilyId(), roleId);
        }
    }

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
        ServiceHelper.roleService().notice(roleId, new FamilyWarEnterSafeSceneEvent(initType, memberType, myFamilyTotalPoints, enemyFamilyTotalPoints, FamilyWarUtil.getNearBattleEndTimeL(ActConst.ID_FAMILY_WAR_LOCAL) - System.currentTimeMillis()));
    }

    /**
     * 进入精英战场
     *
     * @param controlServerId
     * @param mainServerId
     * @param familyId
     * @param roleId
     * @param fighterEntity
     */
    public void enterEliteFight(int controlServerId, int mainServerId, long familyId, long roleId, FighterEntity fighterEntity) {
        // 根据familyId找到对应的battleId，如果familyId/battleId不存在，则提示
        KnockoutFamilyInfo familyInfo = familyMap.get(familyId);
        if (familyInfo == null) {
            roleService.warn(mainServerId, roleId, "no family");
            return;
        }
        LogUtil.info("familywar|进入精英战场,roleId:{},familyMember:{}", roleId, familyInfo.getMemberMap().keySet());
        if (!familyInfo.getTeamSheet().contains(roleId)) {
            roleService.warn(mainServerId, roleId, "not in elite fight team");
            return;
        }
        String battleId = familyInfo.getBattleId();
        if (battleId == null || "".equals(battleId)) {
            roleService.warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_begintext"), FamilyWarUtil.getNearBattleTimeStr(ActConst.ID_FAMILY_WAR_LOCAL)));
            return;
        }
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle != null) {
            if (battle.isEliteFinish()) {
                roleService.warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_desc_eliteover")));
                return;
            }
            if (!battle.isFighting()) {
                long remainTime = ((battle.getLastEndFightTimeStamp() + FamilyActWarManager.familywar_intervaltime * 1000) - System.currentTimeMillis()) / 1000;
                if (remainTime <= 0) {
                    remainTime = 1;
                }
                roleService.warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_nextround"), remainTime));
                //// FIXME: 2017-04-17 这里的下一场时间
                return;
            }
            familyInfo.getMemberMap().get(roleId).setFighterEntity(fighterEntity);
            battle.enterEliteFight(mainServerId, familyId, roleId, fighterEntity);
        } else {
            StringBuilder stringBuilder = TimeUtil.getChinaShow(FamilyWarUtil.getNearBattleTimeL(ActConst.ID_FAMILY_WAR_LOCAL));
            roleService.warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_nexturn"), stringBuilder.toString()));
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
//    @Override
    public void enterNormalFightWaitingQueue(int controlServerId, int mainServerId, long familyId, long roleId, FighterEntity fighterEntity) {
        // 根据familyId找到对应的battleId，如果familyId/battleId不存在，则提示
        KnockoutFamilyInfo familyInfo = familyMap.get(familyId);
        if (familyInfo == null) {
            roleService.warn(mainServerId, roleId, "no family");
            return;
        }
        LogUtil.info("familywar|进入匹配队列,roleId{},familyMember:{}", roleId, familyInfo.getMemberMap().keySet());
        if (!familyInfo.getMemberMap().containsKey(roleId)) {
            roleService.warn(mainServerId, roleId, "not in normal fight team1");
            return;
        }
        String battleId = familyInfo.getBattleId();
        if (battleId == null || "".equals(battleId)) {
            roleService.warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_begintext"), FamilyWarUtil.getNearBattleTimeStr(ActConst.ID_FAMILY_WAR_LOCAL)));
            return;
        }
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle != null) {
            familyInfo.getMemberMap().get(roleId).setFighterEntity(fighterEntity);
            battle.enterNormalFightWaitingQueue(controlServerId, mainServerId, familyId, roleId, fighterEntity);
        } else {
            if (familyInfo.isWinner()) {
                roleService.warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_begintext"), FamilyWarUtil.getNearBattleTimeStr(ActConst.ID_FAMILY_WAR_LOCAL)));
            } else {
                roleService.warn(mainServerId, roleId, String.format(DataManager.getGametext("familywar_tips_fightover")));
            }
            return;
        }
    }

    /**
     * 退去匹配队列
     *
     * @param controlServerId
     * @param mainServerId
     * @param familyId
     * @param roleId
     */
    public void cancelNormalFightWaitingQueue(int controlServerId, int mainServerId, long familyId, long roleId) {
        // 根据familyId找到对应的battleId，如果familyId/battleId不存在，则提示
        KnockoutFamilyInfo familyInfo = familyMap.get(familyId);
        if (familyInfo == null) {
            roleService.warn(mainServerId, roleId, "no family");
            return;
        }
        LogUtil.info("roleId{},familyMember:{}", roleId, familyInfo.getMemberMap().keySet());
        if (!familyInfo.getMemberMap().containsKey(roleId)) {
            roleService.warn(mainServerId, roleId, "not in normal fight team0");
            return;
        }
        String battleId = familyInfo.getBattleId();
        if (battleId == null || "".equals(battleId)) {
            roleService.warn(mainServerId, roleId, "no battle id");
            return;
        }
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle != null) {
            battle.cancelNormalFightWaitingQueue(controlServerId, mainServerId, familyId, roleId);
        } else {
            roleService.warn(mainServerId, roleId, "no battle");
            return;
        }
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
        roleService.send(mainServerId, roleId, new ClientFamilyWarBattleFightPersonalPoint(battle.getBattleNormalPersonalPosints(Long.toString(roleId))));
    }

    public void onEliteFighterAddingSucceeded(int mainServerId, int fightServerId, String battleId, String fightId, long roleId) {
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle == null) return;
        battle.handleFighterEnter(roleId, fightId);
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

    public void match() {
        for (FamilyWarKnockoutBattle battle : battleMap.values()) {
            try {
                battle.match(FamilyWarConst.W_TYPE_LOCAL);
            } catch (Exception e) {
                LogUtil.error("", e);
            }
        }
    }

    /* 发送赛程 */
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
        int thisStep = FamilyWarConst.STEP_OF_SUB_FLOW;
        LogUtil.info("familywar|state:{}", packet.getWarState());
        String text = "";
        switch (thisStep) {
            case FamilyWarKnockoutFlow.STEP_GENERATE_TEAM_SHEET:
                packet.setWarState(ClientFamilyWarUiFixtures.S_SHEET);
                LogUtil.info("familywar|state:{}", packet.getWarState());
                break;
            case FamilyWarKnockoutFlow.STEP_BEFORE_QUARTER_FIANLS:
            case FamilyWarKnockoutFlow.STEP_BEFORE_SEMI_FIANLS:
            case FamilyWarKnockoutFlow.STEP_BEFORE_END_FIANLS:
                packet.setWarState(ClientFamilyWarUiFixtures.S_SHOW_ICON);
                LogUtil.info("familywar|state:{}", packet.getWarState());
                break;
            case FamilyWarKnockoutFlow.STEP_START_QUARTER_FINALS:
            case FamilyWarKnockoutFlow.STEP_START_SEMI_FINALS:
            case FamilyWarKnockoutFlow.STEP_START_FINALS:
                packet.setWarState(ClientFamilyWarUiFixtures.S_ELITE);
                LogUtil.info("familywar|state:{}", packet.getWarState());
                break;
            case FamilyWarKnockoutFlow.STEP_START_KNOCKOUT:
            case FamilyWarKnockoutFlow.STEP_END_QUARTER_FINALS:
            case FamilyWarKnockoutFlow.STEP_END_SEMI_FINALS:
            case FamilyWarKnockoutFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_SEMI:
            case FamilyWarKnockoutFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_FINALS:
                text = String.format(DataManager.getGametext("familywar_tips_nexturn"), FamilyWarUtil.getNearBattleTimeStr(ActConst.ID_FAMILY_WAR_LOCAL));
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
            LogUtil.info("familywar|state:{}", packet.getWarState());
        }
        if (FamilyWarConst.STEP_OF_SUB_FLOW == STEP_END_KNOCKOUT) {
            packet.setWarState(ClientFamilyWarUiFixtures.S_CYCLE_END);
            LogUtil.info("familywar|state:{}", packet.getWarState());
        }
        packet.setWarType(ClientFamilyWarUiFixtures.T_LOCAL);
        packet.setDate(0);
        int nextBattleRemainderTime = 0;
        if (FamilyWarConst.STEP_OF_SUB_FLOW == FamilyWarKnockoutFlow.STEP_GENERATE_TEAM_SHEET
                || FamilyWarConst.STEP_OF_SUB_FLOW == FamilyWarKnockoutFlow.STEP_END_QUARTER_FINALS
                || FamilyWarConst.STEP_OF_SUB_FLOW == FamilyWarKnockoutFlow.STEP_END_SEMI_FINALS) {
            nextBattleRemainderTime = (int) ((FamilyWarUtil.getNearBattleTimeL(ActConst.ID_FAMILY_WAR_LOCAL) - System.currentTimeMillis()) / 1000);
        }
        packet.setNextBattleRemainderTime(nextBattleRemainderTime);
        int timeStep = 0;
        for (int i = 0; i < FamilyActWarManager.timeLinePoint.length; i++) {
            if (FamilyWarConst.STEP_OF_SUB_FLOW >= FamilyActWarManager.timeLinePoint[i]) {
                timeStep = i;
            }
        }
        packet.setText(text);
        packet.setIndexOfTimeline(timeStep + 1);
        packet.set1stFamilyId(_1stFamilyId);
        packet.set2ndFamilyId(_2ndFamilyId);
        packet.set3rdFamilyId(_3rdFamilyId);
        packet.set4thFamilyId(_4thFamilyId);
        packet.setPlayerQualification(selfQualification);
        packet.setFamilyQualification((byte) (familyInfo == null ? 0 : familyInfo.isWinner() ? 1 : 0));
        packet.setAgenda(fixtures);
        for (KnockoutFamilyInfo info : familyMap.values()) {
            packet.addFamilyInfo(info.getFamilyId(), info.getFamilyName(), info.getMainServerId());
        }
        roleService().send(mainServerId, roleId, packet);
    }

    public void sendUpdatedFixtures(int mainServerId, long familyId, long roleId) {

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
        List<String> componentName = new ArrayList<>();
        componentName.add(MConst.Role);
        componentName.add(MConst.Skill);
        componentName.add(MConst.Deity);
        componentName.add(MConst.Buddy);
        // 增加已申请人的列表
        for (long applicantId : familyInfo.getApplicationSheet()) {
            KnockoutFamilyMemberInfo applicantInfo = familyInfo.getMemberMap().get(applicantId);
            if (applicantInfo == null) {
                continue;
            }
            Summary summary = ServiceHelper.summaryService().getSummary(applicantInfo.getMemberId());
            if (summary == null || isDummy(summary, componentName)) continue;
//            ForeShowSummaryComponent fsSummary = (ForeShowSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(summary.getRoleId(), MConst.ForeShow);
//            if (!fsSummary.isOpen(ForeShowConst.FAMILYFIGHT)) continue;
            packet.addApplicant(applicantId, applicantInfo.getName(), applicantInfo.getPostId(),
                    applicantInfo.getLevel(), applicantInfo.getFightScore(), summary.getOfflineTimestamp(), summary.isOnline(),
                    familyInfo.getTeamSheet().contains(applicantId) ? K_APP_QUAL_ELITE : K_APP_QUAL_NORMAL);
        }
        packet.setLock(Packet.FALSE);
        roleService.send(mainServerId, roleId, packet);
    }

    private boolean isDummy(Summary summary, List<String> componentName) {
        for (Map.Entry<String, SummaryComponent> componentEntry : summary.getComponentMap().entrySet()) {
            if (componentName.contains(componentEntry.getKey())) {
                if (componentEntry.getValue().isDummy()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void applyEliteFightSheet(int mainServerId, long familyId, long roleId) {
        // 判断阶段
        // TODO: 2017-05-04 改名单
        if (!canApply(FamilyWarConst.STEP_OF_SUB_FLOW)) {
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

    private boolean canApply(int step) {
        switch (step) {
            case FamilyWarKnockoutFlow.STEP_START_KNOCKOUT:
            case FamilyWarKnockoutFlow.STEP_END_QUARTER_FINALS:
            case FamilyWarKnockoutFlow.STEP_END_SEMI_FINALS:
            case FamilyWarKnockoutFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_SEMI:
            case FamilyWarKnockoutFlow.STEP_SENDAWARD_RESETPOINTS_NOTICE_MASTER_FINALS:
                return true;
            default:
                return false;
        }
    }

    public void cancelApplyEliteFightSheet(int mainServerId, long familyId, long roleId) {
        // 判断阶段
        if (!canApply(FamilyWarConst.STEP_OF_SUB_FLOW)) {
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
        ServiceHelper.emailService().sendToSingle(familyInfo.getMasterId(), emailTemplateIdOfCancelFromTeamSheet, 0L, "系统", null, name);
        sendApplicationSheet(mainServerId, familyId, roleId);
    }

    public void confirmTeamSheet(int mainServerId, long familyId, long verifierId, Set<Long> newTeamSheet) {
        // 判断阶段
        if (!canApply(FamilyWarConst.STEP_OF_SUB_FLOW)) {
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
        sendMainIcon(MultiServerHelper.getServerId(), verifierId, familyId, FamilyWarConst.STATE_ICON_DISAPPEAR, 0, FamilyWarConst.W_TYPE_LOCAL);
        hasNoticeMasterMap.put(familyInfo.getMasterId(), true);
        StringBuilder roleNames = new StringBuilder();
        KnockoutFamilyMemberInfo memberInfo;
        for (long roleId : newTeamSheet) {
            memberInfo = familyInfo.getMemberMap().get(roleId);
            if (memberInfo == null) continue;
            roleNames.append(memberInfo.getName()).append("\\n");
        }
        String familyMsg = String.format(DataManager.getGametext("familywar_desc_familywarchat"), roleNames.toString());
        ServiceHelper.chatService().chat("系统", ChatManager.CHANNEL_FAMILY, 0L, familyId, familyMsg, false);
        familyWarLocalService().sendTeamSheetChangedEmail(mainServerId, familyId, addTeamSheet, delTeamSheet);
    }

//    public void sendMainIconToMaster() {
//        LogUtil.info("familywar|给族长发设置名单的icon,失败的family:{},通知与否的集合:{}", failFamilySet, hasNoticeMasterMap);
//        for (KnockoutFamilyInfo familyInfo : familyMap.values()) {
//            LogUtil.info("familywar|0给族长发设置名单的icon|familyid:{},族长:{}", familyInfo.getFamilyId(), familyInfo.getMasterId());
//            if (failFamilySet.contains(familyInfo.getFamilyId()))
//                continue;
//            LogUtil.info("familywar|1给族长发设置名单的icon|familyid:{},族长:{}", familyInfo.getFamilyId(), familyInfo.getMasterId());
//            if (hasNoticeMasterMap.get(familyInfo.getMasterId()))
//                continue;
//            LogUtil.info("familywar|2给族长发设置名单的icon|familyid:{},族长:{}", familyInfo.getFamilyId(), familyInfo.getMasterId());
//            sendMainIcon(MultiServerHelper.getServerId(), familyInfo.getMasterId(), FamilyWarConst.STATE_NOTICE_MASTER, 0L);
//        }
//    }

//    private void sendMainIcon(int mainServer, long roleId, int state, long countdown) {
//        ClientFamilyWarMainIcon mainIcon = new ClientFamilyWarMainIcon(state, countdown);
//        long familyId = ServiceHelper.familyRoleService().getFamilyId(roleId);
//        mainIcon.setQualification(isKnockoutFamily(familyId) ? FamilyWarConst.WITH_QUALIFICATION : FamilyWarConst.WITHOUT_QUALIFICATION);
//        ServiceHelper.roleService().send(roleId, mainIcon);
//    }

    private Set<Long> union(Set<Long> s1, Set<Long> s2) {
        Set<Long> set = new HashSet<>();
        set.addAll(s1);
        set.addAll(s2);
        return set;
    }

    private Set<Long> intersection(Set<Long> s1, Set<Long> s2) {
        Set<Long> set = new HashSet<>();
        for (Long l : s1) {
            if (s2.contains(l)) {
                set.add(l);
            }
        }
        return set;
    }

//    /**
//     * 发送积分排行榜信息
//     *
//     * @param mainServerId
//     * @param roleId
//     * @param subtype
//     */
//    public void sendPointsRank(int mainServerId, long roleId, byte subtype) {
//        IndexList rankList = null;
//        Map<String, Long> pointsMap = null;
//        switch (subtype) {
//            case ServerFamilyWarUiPointsRank.SUBTYPE_ELITE_FIGHT:
//                rankList = this.elitePointsRankList;
//                pointsMap = this.elitePointsMap;
//                break;
//            case ServerFamilyWarUiPointsRank.SUBTYPE_NORMAL_FIGHT:
//                rankList = this.normalPointsRankList;
//                pointsMap = this.normalPointsMap;
//                break;
//            default:
//                return;
//        }
//        ClientFamilyWarUiPointsRank packet = new ClientFamilyWarUiPointsRank(
//                subtype, Packet.FALSE, getPktAuxFamilyWarPointsObjList(rankList, 100));
//        RankObj myRankObj = rankList.getRankObjByKey(Long.toString(roleId));
//        packet.setMyRank(rankList.getRank(Long.toString(roleId)));
//        packet.setMyRankObj(createPointsObj(myRankObj));
//        roleService().send(mainServerId, roleId, packet);
//    }
//
//    /**
//     * 构建排行榜信息类，可能返回null
//     *
//     * @param rankObj
//     * @return
//     */
//    private PktAuxFamilyWarPointsObj createPointsObj(RankObj rankObj) {
//        if (rankObj == null) return null;
//        FamilyWarPointsRankObj rankObj0 = (FamilyWarPointsRankObj) rankObj;
//        return new PktAuxFamilyWarPointsObj(
//                Long.parseLong(rankObj.getKey()), rankObj0.getRoleName(), rankObj0.getFamilyName(), rankObj0.getServerId(), rankObj.getPoints()); //
//    }
//
//    /**
//     * 获取排行前n名的信息
//     *
//     * @param rankList
//     * @param n
//     * @return
//     */
//    private List<PktAuxFamilyWarPointsObj> getPktAuxFamilyWarPointsObjList(IndexList rankList, int n) {
//        List<PktAuxFamilyWarPointsObj> list = new ArrayList<>();
//        List<RankObj> top100List = rankList.getTop(100);
//        for (RankObj rankObj : top100List) {
//            PktAuxFamilyWarPointsObj pointsObj = createPointsObj(rankObj);
//            if (pointsObj != null) {
//                list.add(pointsObj);
//            }
//        }
//        return list;
//    }

    public final void onClientPreloadFinished(int mainServerId, String battleId, String fightId, long roleId) {
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle != null) {
            battle.onClientPreloadFinished(mainServerId, fightId, roleId);
        } else {
            LogUtil.info("familywar|onClientPreloadFinished: no such battle {}", battleId);
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

    public void handleFighterQuit(String battleId, long roleId, String fightId) {
        FamilyWarKnockoutBattle battle = battleMap.get(battleId);
        if (battle != null) {
            battle.handleFighterQuit(roleId, fightId);
        }
    }

    public void removeBattle(String battleId, int battleType) {
        switch (battleType) {
            case K_BATTLE_TYPE_QUARTER:
                quarterFinalBattleMap.remove(battleId);
                break;
            case K_BATTLE_TYPE_SEMI:
                semiFinalBattleMap.remove(battleId);
                break;
            case K_BATTLE_TYPE_FINAL:
            case K_BATTLE_TYPE_FINAL_3RD4TH:
                finalBattleMap.remove(battleId);
                break;
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

    /**
     * 处理轮空的家族，直接晋级
     *
     * @param battleType
     * @param winnerFamilyId
     */
    public void handleEmptyBattle(int battleType, long winnerFamilyId, long loserFamilyId) {
        KnockoutFamilyInfo winnerFamilyInfo = familyMap.get(winnerFamilyId);
        LogUtil.info("familywar|淘汰赛[{}]，{}胜利", "emptyBattle", winnerFamilyInfo.getFamilyName());
        if (battleType == K_BATTLE_TYPE_QUARTER || battleType == K_BATTLE_TYPE_SEMI) {
            // 生成下一阶段的对战名额
            int nextSep = winnerFamilyInfo.getSeq() / 2 - 1;
            fixtures[nextSep] = winnerFamilyId;
            winnerFamilyInfo.setSeq(nextSep);
            if (battleType == K_BATTLE_TYPE_SEMI) {
                KnockoutFamilyInfo loserFamilyInfo = familyMap.get(loserFamilyId);
                fixtures[nextSep + 14] = loserFamilyId;
                loserFamilyInfo.setSeq(nextSep + 14);
            }
        } else if (battleType == K_BATTLE_TYPE_FINAL) { // 决赛
            _1stFamilyId = winnerFamilyId;
        } else if (battleType == K_BATTLE_TYPE_FINAL_3RD4TH) { // 3/4名决赛
            _3rdFamilyId = winnerFamilyId;
        }
    }

    /**
     * fixme:进阶这里需要处理
     *
     * @param battleId
     * @param winnerFamilyId
     * @param loserFamilyId
     */
    public void finishBattle(String battleId, long winnerFamilyId, long loserFamilyId) {
        FamilyWarKnockoutBattle battle = battleMap.remove(battleId);
        if (battle == null) {
            LogUtil.error("familywar|不存在对战, battleId=" + battleId);
            return;
        }
        int ranking = 0;//名次
        KnockoutFamilyInfo winnerFamilyInfo = familyMap.get(winnerFamilyId);
        KnockoutFamilyInfo loserFamilyInfo = familyMap.get(loserFamilyId);
        LogUtil.info("familywar|淘汰赛[{}]，{}胜利，{}失败", battleId, winnerFamilyInfo.getFamilyName(), loserFamilyInfo.getFamilyName());
        int battleType = battle.getType();
        LogUtil.info("familywar|battleType:{},winSeq:{},loseSeq:{}", battleType, winnerFamilyInfo.getSeq(), loserFamilyInfo.getSeq());
        if (battleType == K_BATTLE_TYPE_QUARTER || battleType == K_BATTLE_TYPE_SEMI) {
            // 生成下一阶段的对战名额
            int nextSep = winnerFamilyInfo.getSeq() / 2 - 1;
            fixtures[nextSep] = winnerFamilyId;
            winnerFamilyInfo.setSeq(nextSep);
            winnerFamilyInfo.setWinner(true);
            loserFamilyInfo.setWinner(false);
            LogUtil.info("familywar|nextSeq:{},win:{},lose:{},fixture:{}", nextSep, winnerFamilyId, loserFamilyId, fixtures);
            if (battleType == K_BATTLE_TYPE_SEMI) { // 3-4名决赛名单，特殊处理
                fixtures[nextSep + 14] = loserFamilyId;
                loserFamilyInfo.setSeq(nextSep + 14);
                winnerFamilyInfo.setWinner(true);
                loserFamilyInfo.setWinner(true);
            }
        } else if (battleType == K_BATTLE_TYPE_FINAL) { // 决赛
            _1stFamilyId = winnerFamilyId;
            _2ndFamilyId = loserFamilyId;
            winnerFamilyInfo.setWinner(false);
            loserFamilyInfo.setWinner(false);
            String message = String.format(FamilyActWarManager.familywar_roll_winer, winnerFamilyInfo.getFamilyName());
            LogUtil.info("familywar|跑马灯信息:{}", message);
            ServiceHelper.chatService().chat("系统", ChatManager.CHANNEL_SYSTEM, 0L, 0L, message, false);
            ServiceHelper.chatService().announce(message);
//            MainRpcHelper.familyWarService().updateFamilyWarData(familyMap.get(_1stFamilyId).getMainServerId(), FamilyWarConst.W_TYPE_LOCAL, _1stFamilyId, 1);
//            MainRpcHelper.familyWarService().updateFamilyWarData(familyMap.get(_2ndFamilyId).getMainServerId(), FamilyWarConst.W_TYPE_LOCAL, _2ndFamilyId, 2);
        } else if (battleType == K_BATTLE_TYPE_FINAL_3RD4TH) { // 3/4名决赛
            _3rdFamilyId = winnerFamilyId;
            _4thFamilyId = loserFamilyId;
            winnerFamilyInfo.setWinner(false);
            loserFamilyInfo.setWinner(false);
//            MainRpcHelper.familyWarService().updateFamilyWarData(familyMap.get(_3rdFamilyId).getMainServerId(), FamilyWarConst.W_TYPE_LOCAL, _3rdFamilyId, 3);
//            MainRpcHelper.familyWarService().updateFamilyWarData(familyMap.get(_4thFamilyId).getMainServerId(), FamilyWarConst.W_TYPE_LOCAL, _4thFamilyId, 4);
        }
        if (!winnerFamilyInfo.isWinner()) {
            addFailFamily(winnerFamilyInfo.getFamilyId());
        }
        if (!loserFamilyInfo.isWinner()) {
            addFailFamily(loserFamilyInfo.getFamilyId());
        }
        updateFamilyFixture();
    }

    public void removeBattle() {
        battleMap.clear();
    }

    private String getSeqMark(int seq) {
        return K_SEQ_MARK[seq];
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


    // fixme: 入库
    public void acquireMinPointsAward(int mainServerId, long roleId, long acquirePoints) {
        if (!memberMap.containsKey(roleId)) { // 非家族成员则不发奖
//            roleService().warn(mainServerId, roleId, "非家族成员则不发奖");
            return;
        }
        if (elitePointsMap.containsKey(Long.toString(roleId))) {
            Set<Long> recordSet = eliteMinPointsAwardAcquiredRecordSet.get(roleId);
            if (recordSet == null) {
                recordSet = new HashSet<>();
                eliteMinPointsAwardAcquiredRecordSet.put(roleId, recordSet);
            }
            acquireMinPointsAward0(mainServerId, roleId, acquirePoints,
                    elitePointsMap, recordSet, eliteMinPointsAwardMap, FamilyWarConst.MIN_AWARD_ELITE);
        } else if (normalPointsMap.containsKey(Long.toString(roleId))) {
            Set<Long> recordSet = normalMinPointsAwardAcquiredRecordSet.get(roleId);
            if (recordSet == null) {
                recordSet = new HashSet<>();
                normalMinPointsAwardAcquiredRecordSet.put(roleId, recordSet);
            }
            acquireMinPointsAward0(mainServerId, roleId, acquirePoints,
                    normalPointsMap, recordSet, normalMinPointsAwardMap, FamilyWarConst.MIN_AWARD_NORMAL);
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
        localService.sendAward(mainServerId, roleId, EventType.FAMILY_WAR_PERSONAL_POINT.getCode(), emailTemplateIdOfMinPointsAward, toolMap);
        recordSet.add(acquirePoints);
        ClientFamilyWarUiMinPointsAward packet = new ClientFamilyWarUiMinPointsAward(
                ClientFamilyWarUiMinPointsAward.SUBTYPE_ACQUIRE, awardType, acquirePoints, recordSet);
        roleService().send(mainServerId, roleId, packet);
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
        roleService().notice(roleId, new FamilyWarSupportEvent(FamilyWarConst.W_TYPE_LOCAL));
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

    public void setFightService(FightBaseService fightService) {
        this.fightService = fightService;
    }

    public FightBaseService fightService() {
        return fightService;
    }

    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    public RoleService roleService() {
        return roleService;
    }

    public void setFamilyWarLocalService(FamilyWarLocalService familyWarLocalService) {
        this.familyWarLocalService = familyWarLocalService;
    }

    public FamilyWarLocalService familyWarLocalService() {
        return familyWarLocalService;
    }

    public LocalService getLocalService() {
        return localService;
    }

    public void setLocalService(LocalService localService) {
        this.localService = localService;
    }

    public Map<Long, KnockoutFamilyInfo> getFamilyMap() {
        return familyMap;
    }

    public Map<String, FamilyWarKnockoutBattle> getBattleMap() {
        return battleMap;
    }

    public Map<Long, KnockoutFamilyMemberInfo> getMemberMap() {
        return memberMap;
    }

    public IndexList getElitePointsRankList() {
        return elitePointsRankList;
    }

    public IndexList getNormalPointsRankList() {
        return normalPointsRankList;
    }
}

