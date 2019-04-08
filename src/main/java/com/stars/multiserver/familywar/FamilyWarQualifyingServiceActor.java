package com.stars.multiserver.familywar;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.activityflow.ActivityFlow;
import com.stars.core.activityflow.ActivityFlowUtil;
import com.stars.core.persist.DbRowDao;
import com.stars.ExcutorKey;
import com.stars.core.schedule.SchedulerManager;
import com.stars.core.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.modules.familyactivities.war.event.FamilyWarFighterAddingSucceededEvent;
import com.stars.modules.familyactivities.war.packet.ClientFamilyWarMainIcon;
import com.stars.modules.familyactivities.war.packet.ui.ClientFamilyWarUiFixtures;
import com.stars.modules.familyactivities.war.packet.ui.ClientFamilyWarUiMinPointsAward;
import com.stars.modules.familyactivities.war.prodata.FamilyWarRankAwardVo;
import com.stars.modules.pk.packet.ServerOrder;
import com.stars.modules.rank.RankManager;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFamilyWarEliteFight;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterPK;
import com.stars.modules.scene.prodata.DynamicBlock;
import com.stars.modules.scene.prodata.MonsterAttributeVo;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.flow.FamilyWarFlow;
import com.stars.multiserver.familywar.flow.FamilyWarQualifyingFlow;
import com.stars.multiserver.familywar.knockout.FamilyWarKnockoutBattle;
import com.stars.multiserver.familywar.knockout.KnockoutFamilyInfo;
import com.stars.multiserver.familywar.qualifying.FamilyWarQualifying;
import com.stars.multiserver.familywar.qualifying.cache.FamilyWarQualifyingFixtureCache;
import com.stars.multiserver.familywar.rank.RankConst;
import com.stars.multiserver.familywar.remote.FamilyWarRemoteFamily;
import com.stars.multiserver.fight.ServerOrders;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.activities.ActConst;
import com.stars.services.family.FamilyConst;
import com.stars.services.family.main.userdata.FamilyMemberPo;
import com.stars.services.rank.prodata.RankAwardVo;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.FamilyRankPo;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.util.TimeUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.stars.modules.familyactivities.war.FamilyActWarManager.*;

/**
 * Created by chenkeyu on 2017-05-18.
 */
public class FamilyWarQualifyingServiceActor extends ServiceActor implements FamilyWarQualifyingService {

    private Map<Integer, Boolean> allServerMap;//serverId , havQualify or no
    private FamilyWarQualifying qualifying;
    private Map<Integer, Boolean> familyWarServerMap;//serverId,initialization or not
    private List<AbstractRankPo> familyList;
    private Map<Long, Integer> familyServerId;//familyId,serverId
    private long startTimestamp = 0L;
    private int check = 0;
    private boolean initialization = false;
    private long checkTimeStamp = 0L;
    private Map<Integer, List<Long>> groupIdfamilyIdsMap;//groupId,list of familyId
    private Map<Long, Integer> familyIdToGroupId;//familyId--groupId
    private Map<Integer, Map<Integer, List<FamilyWarQualifyingFixtureCache>>> fixtureCacheMap;//day , groupId, list of fixtrueCache
    private Map<Long, List<FamilyWarQualifyingFixtureCache>> familyIdCacheList;//familyId , list of cache
    private DbRowDao rowDao;
    private FamilyWarQualifyingFlow qualifyingFlow;

    private List<FamilyWarQualifyingFixtureCache> cacheList;//拉起专用

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.FamilyWarQualifyingService, this);
        qualifyingFlow = new FamilyWarQualifyingFlow();
        qualifyingFlow.init(SchedulerHelper.getScheduler(), DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_QUALIFYING));
        rowDao = new DbRowDao(SConst.FamilyWarQualifyingService, DBUtil.DB_COMMON);
        familyWarServerMap = new HashMap<>();
        familyList = new ArrayList<>();
        familyServerId = new HashMap<>();
        this.fixtureCacheMap = new HashMap<>();
        familyIdCacheList = new HashMap<>();
        allServerMap = new HashMap<>();
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.FamilyWar, new Runnable() {
            @Override
            public void run() {
                syncBattleFightUpdateInfo();
            }
        }, 10, 1, TimeUnit.SECONDS);
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.FamilyWar, new Runnable() {
            @Override
            public void run() {
                ServiceHelper.familyWarQualifyingService().checkAndEndTimeout();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }


    @Override
    public void printState() {

    }


    @Override
    public void registerFamilyWarServer(int serverId, int comFrom) {
        allServerMap.put(comFrom, false);

    }

    @Override
    public void qualifyFamilyWarServer(int serverId, int comFrom) {
        allServerMap.put(comFrom, true);
        familyWarServerMap.put(comFrom, false);
        LogUtil.info("server:{}| {} register to FamilyWarServer havQualify|familyWarServer:{}", serverId, comFrom, familyWarServerMap);
//        FamilyWarRpcHelper.familyWarService().changeServerState(comFrom, FamilyWarConst.noneQulification);
    }

    @Override
    public void containFamily(int serverId, int fromServerId, long familyId, long roleId) {
        if (qualifying != null) {
            qualifying.containFamily(fromServerId, familyId, roleId);
        } else {
            FamilyWarRpcHelper.familyWarService().containFamily(fromServerId, familyId, roleId, FamilyWarConst.waitQulification);
        }
    }

    @Override
    public void generateFamilyData(int serverId, int fromServerId, List<AbstractRankPo> familyList) {
        if (startTimestamp == 0L) {
            startTimestamp = System.currentTimeMillis();
        }
        if (familyWarServerMap.get(fromServerId)) {
            LogUtil.info("familywar|家族数据重复了");
            return;
        }
        if (initialization)
            return;
        familyWarServerMap.put(fromServerId, true);
        for (AbstractRankPo rankPo : familyList) {
            this.familyServerId.put(rankPo.getUniqueId(), fromServerId);
        }
        this.familyList.addAll(familyList);
        boolean isAllConnect = isAllConnect();
        LogUtil.info("familywar| {} 服务连接成功, 是否全部连接成功:{}|服务连接情况:{}", fromServerId, isAllConnect, familyWarServerMap);
        if (isAllConnect) {
            LogUtil.info("familywar|所有服务器数据准备完毕，触发家族战海选赛流程");
            doAllServerConnect();
        }
    }

    private void doAllServerConnect() {
        Collections.sort(this.familyList);
        // TODO: 2017-05-19 全部取完资格
        int count = getFamilyCount();
        FamilyWarConst.familyCount = count;
        LogUtil.info("familywar|count:{},familySize:{}", count, familyList.size());
        if (count > familyList.size()) {
            // FIXME: 2017-06-08 这里有个坑，familyDataList的数量可能不足（由于要到各个游戏服拿数据，如果没有返回的话，就会报错）
            List<AbstractRankPo> tmpList = new ArrayList<>();
            int lack = count - familyList.size();
            for (int i = 0; i < lack; i++) {
                FamilyRankPo rankPo = new FamilyRankPo(0L);
                tmpList.add(rankPo);
            }
            familyList.addAll(tmpList);
            LogUtil.info("familywar| warn 缺了 {} 个家族 , count:{} , familySize:{}", lack, count, familyList.size());
        }
        List<AbstractRankPo> familyDataList = familyList.subList(0, count);
        generateGroup(count, familyDataList);
        generateFixture();
        this.familyList = null;
        initialization = true;
        qualifying.newfamilyIdCacheList(familyIdCacheList);
        qualifying.newfamilyIdToGroupId(familyIdToGroupId);
        qualifying.newGroupIdFamilyIdsMap(groupIdfamilyIdsMap);
        qualifying.newfixtureCacheMap(fixtureCacheMap);
        onCallLocalService();
    }

    private boolean isAllConnect() {
        for (Boolean aBoolean : familyWarServerMap.values()) {
            if (!aBoolean) {
                LogUtil.info("familywar|服务器连接情况:{}", familyWarServerMap);
                return false;
            }
        }
        return true;
    }

    private void checkServerConnect() {
        if (isAllConnect())
            return;
        check++;
        if ((System.currentTimeMillis() - startTimestamp) / 1000 > 60) {
            if (check >= 60) {
                List<Integer> unConnectServerList = new ArrayList<>();
                for (Map.Entry<Integer, Boolean> entry : familyWarServerMap.entrySet()) {
                    if (!entry.getValue()) {
                        unConnectServerList.add(entry.getKey());
                    }
                }
                LogUtil.info("familywar|距离取资格已经过去 {} 秒，尝试重新连接 :{} 服务的家族数据", (System.currentTimeMillis() - startTimestamp) / 1000, unConnectServerList);
                for (Integer id : unConnectServerList) {
                    FamilyWarRpcHelper.familyWarService().startQualifying(id);
                }
                check = 0;
            }
        }
        if ((System.currentTimeMillis() - startTimestamp) / 1000 > 1200 && !initialization) {
            LogUtil.info("familywar|超时触发家族战跨服海选赛流程 familyWarServerMap:{}", familyWarServerMap);
            doAllServerConnect();
        }
    }

    @Override
    public void onRankFamilyData(int serverId, int fromServerId, List<KnockoutFamilyInfo> infoList) {
        generateQualifying(infoList, qualifying);
    }

    @Override
    public void startBattle(int serverId, int battleType) {
        qualifying.startBattle(battleType);
    }

    @Override
    public void endBattle(int serverId, int battleType) {
        qualifying.endBattle(battleType);
    }

    public void updateIconText(int serverId, String text) {
        LogUtil.info("familywar|发送活动icon文本text:{}", text);
        for (int id : familyWarServerMap.keySet()) {
            if (FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW == FamilyWarQualifyingFlow.STEP_END_5TH) {
                String time = FamilyWarUtil.getBattleTimeStr(FamilyWarQualifyingFlow.STEP_END_QUALIFYING, ActConst
                        .ID_FAMILY_WAR_QUALIFYING);
                String tmpStr = String.format(DataManager.getGametext("familywar_desc_awardtime"), time);
                FamilyWarRpcHelper.familyWarService().setOptions(id, ActConst.ID_FAMILY_WAR_GENERAL,
                        FamilyConst.ACT_BTN_MASK_DISPLAY | FamilyConst.ACT_BTN_MASK_LIGHT, -1, tmpStr);
                continue;
            }
            if (FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW == FamilyWarQualifyingFlow.STEP_END_QUALIFYING) {
                FamilyWarRpcHelper.familyWarService().setOptions(id, ActConst.ID_FAMILY_WAR_GENERAL,
                        FamilyConst.ACT_BTN_MASK_DISPLAY | FamilyConst.ACT_BTN_MASK_LIGHT, -1, "已结束");
                continue;
            }
            if (!text.equals("")) {
                FamilyWarRpcHelper.familyWarService().setOptions(id, ActConst.ID_FAMILY_WAR_GENERAL,
                        FamilyConst.ACT_BTN_MASK_DISPLAY | FamilyConst.ACT_BTN_MASK_LIGHT, -1, text);
            } else {
                long time = FamilyWarUtil.getNearBattleTimeL(ActConst.ID_FAMILY_WAR_QUALIFYING);
                StringBuilder tmpStr0 = TimeUtil.getChinaShow(time);
                String tmpStr = String.format(DataManager.getGametext("familywar_desc_fightbegintime"), tmpStr0.toString());
                FamilyWarRpcHelper.familyWarService().setOptions(id, ActConst.ID_FAMILY_WAR_GENERAL,
                        FamilyConst.ACT_BTN_MASK_DISPLAY | FamilyConst.ACT_BTN_MASK_LIGHT, -1, tmpStr);
                LogUtil.info("familywar|serverId:{},timStr:{}", id, tmpStr);
            }
        }
    }

    @Override
    public void enterSafeScene(int serverId, int fromServerId, long roleId) {
        LogUtil.info("familywar|进入备战场景|总赛程阶段:{}|子赛程阶段:{}", FamilyWarConst.STEP_OF_GENERAL_FLOW, FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW);
        if (FamilyWarConst.STEP_OF_GENERAL_FLOW == FamilyWarFlow.STEP_REMOTE_QUALIFYING_START) {
            qualifying.enterSafeScene(serverId, fromServerId, roleId);
        }
    }

    @Override
    public void enterSafeScene(int serverId, int fromServerId, long familyId, long roleId) {
        LogUtil.info("familywar|进入备战场景|总赛程阶段:{}|子赛程阶段:{}", FamilyWarConst.STEP_OF_GENERAL_FLOW, FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW);
        if (FamilyWarConst.STEP_OF_GENERAL_FLOW == FamilyWarFlow.STEP_REMOTE_QUALIFYING_START) {
            qualifying.enterSafeScene(serverId, fromServerId, familyId, roleId);
        }
    }

    @Override
    public void enterFight(int serverId, int fromServerId, long familyId, long roleId, FighterEntity fighterEntity) {
        LogUtil.info("familywar|进入战斗场景|总赛程阶段:{}|子赛程阶段:{}", FamilyWarConst.STEP_OF_GENERAL_FLOW, FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW);
        if (FamilyWarConst.STEP_OF_GENERAL_FLOW == FamilyWarFlow.STEP_REMOTE_QUALIFYING_START) {
            LogUtil.info("familywar|serverActor==家族:{}的玩家:{}进入战场", familyId, roleId);
            qualifying.enter(serverId, fromServerId, familyId, roleId, fighterEntity);
        }
    }

    @Override
    public void cancelFight(int serverId, int fromServerId, long familyId, long roleId) {
        if (FamilyWarConst.STEP_OF_GENERAL_FLOW == FamilyWarFlow.STEP_REMOTE_QUALIFYING_START) {
            qualifying.cancelNormalFightWaitingQueue(serverId, fromServerId, familyId, roleId);
        }
    }

    @Override
    public void sendMainIcon2All(int serverId, long countdown) {
        int iconState = FamilyWarUtil.getMainIconStateByQualifyingFlowStep();
        LogUtil.info("familywar|赛程时间触发，给所有符合条件的玩家发送图标入口 iconState:{}", iconState);
        if (iconState == ActivityFlow.STEP_START_CHECK) return;
        sendMainIcon(MultiServerHelper.getServerId(), iconState, countdown);
    }

    /**
     * 给所有玩家下发icon
     *
     * @param mainServer
     * @param state
     * @param countdown
     */
    public void sendMainIcon(int mainServer, int state, long countdown) {
        ClientFamilyWarMainIcon mainIcon = new ClientFamilyWarMainIcon(state, countdown);
        for (int serverId : familyWarServerMap.keySet()) {
            FamilyWarRpcHelper.familyWarService().sendMainIcon(serverId, mainIcon, new HashSet<>(qualifying.getFamilyMap().keySet()), qualifying.getFailFamilySet());
        }
    }

    /**
     * 给单个玩家下发icon
     *
     * @param mainServer
     * @param roleId
     * @param state
     * @param countdown
     */
    public void sendMainIcon(int mainServer, long roleId, int state, long countdown, long familyId) {
        ClientFamilyWarMainIcon mainIcon = new ClientFamilyWarMainIcon(state, countdown);
        mainIcon.setQualification(qualifying.isKnockoutFamily(familyId) ? FamilyWarConst.WITH_QUALIFICATION : FamilyWarConst.WITHOUT_QUALIFICATION);
        FamilyWarRpcHelper.roleService().send(mainServer, roleId, mainIcon);
    }

    @Override
    public void sendMainIcon2Role(int serverId, int fromServerId, long roleId, boolean isMaster, long familyId) {
        int iconState = FamilyWarUtil.getMainIconStateByQualifyingFlowStep();
        if (iconState == ActivityFlow.STEP_START_CHECK) return;
        long countdown = 0;
        if (iconState == FamilyWarConst.STATE_START) {
            Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_QUALIFYING);
            if (StringUtil.isEmpty(flowMap) || !flowMap.containsKey(FamilyWarQualifyingFlow.STEP_START_1ST))
                return;
            countdown = ActivityFlowUtil.remainder(System.currentTimeMillis(), flowMap.get(FamilyWarQualifyingFlow.STEP_START_1ST));
        }
        if (iconState != FamilyWarConst.STATE_NOTICE_MASTER) {
            sendMainIcon(fromServerId, roleId, iconState, countdown, familyId);
        }
        if (isMaster && (iconState == FamilyWarConst.STATE_ICON_DISAPPEAR || iconState == FamilyWarConst.STATE_NOTICE_MASTER)) {
            if (FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW != FamilyWarQualifyingFlow.STEP_END_QUALIFYING) {
                qualifying.sendMainIconToMaster(FamilyWarConst.W_TYPE_QUALIFYING, familyId);
            }
        }
    }

    @Override
    public void onPremittedToEnter(int mainServerId, int fightServerId, String fightId, byte camp, long roleId) {
        StageinfoVo stageVo = SceneManager.getStageVo(FamilyActWarManager.stageIdOfEliteFight);
        FighterEntity fighterEntity = qualifying.getMemberMap().get(roleId).getFighterEntity();
        fighterEntity.setCamp(camp);
        if (camp == FamilyWarConst.K_CAMP1) {
            fighterEntity.setPosition(stageVo.getPosition());
            fighterEntity.setRotation(stageVo.getRotation());
        } else {
            fighterEntity.setPosition(stageVo.getEnemyPos(0));
            fighterEntity.setRotation(stageVo.getEnemyRot(0));
        }
        List<FighterEntity> list = new ArrayList<>();
        list.add(fighterEntity);
        ArrayList<String> entityKey = new ArrayList<>();
        entityKey.add(fighterEntity.getUniqueId());
        ServerOrder order = ServerOrders.newAiOrder(ServerOrder.CLOSE_AI, entityKey);
        LogUtil.info("添加玩家成功");
        FamilyWarRpcHelper.fightBaseService().addFighter(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT, MultiServerHelper.getServerId(), fightId, list);
        FamilyWarRpcHelper.fightBaseService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                MultiServerHelper.getServerId(), fightId, order);
    }

    @Override
    public void onFighterAddingSucceeded(int serverId, int mainServerId, int fightServerId, String battleId, String
            fightId, byte camp, long roleId) {
        if (qualifying.getFighterReviveState(battleId, fightId, Long.toString(roleId)) == 1) return;
        // 发进入包
        ClientEnterFamilyWarEliteFight packet = new ClientEnterFamilyWarEliteFight();
        packet.setFightType(SceneManager.SCENETYPE_FAMILY_WAR_ELITE_FIGHT);
        packet.setStageId(FamilyActWarManager.stageIdOfEliteFight);
        packet.setLimitTime(getBattleEndRemainderTime(battleId));
        packet.setStartRemainderTime(getDynamicBlockRemainderTime(battleId));
        StageinfoVo stageVo = SceneManager.getStageVo(FamilyActWarManager.stageIdOfEliteFight);
        FighterEntity fighterEntity = qualifying.getMemberMap().get(roleId).getFighterEntity();
        fighterEntity.setCamp(camp);
        if (camp == FamilyWarConst.K_CAMP1) {
            fighterEntity.setPosition(stageVo.getPosition());
            fighterEntity.setRotation(stageVo.getRotation());
        } else {
            fighterEntity.setPosition(stageVo.getEnemyPos(0));
            fighterEntity.setRotation(stageVo.getEnemyRot(0));
        }
        List<FighterEntity> list = new ArrayList<>();
        list.add(fighterEntity);
        packet.setFighterEntityList(list);
        Map<String, FighterEntity> nonPlayerEntity = getMonsterFighterEntity(stageIdOfEliteFight);
        packet.addMonsterVoMap(stageVo.getMonsterVoMap());
        /* 动态阻挡数据 */
        Map<String, Byte> blockStatus = new HashMap<>();
        for (DynamicBlock dynamicBlock : stageVo.getDynamicBlockMap().values()) {
            blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_CLOSE);
            if (dynamicBlock.getShowSpawnId() == 0 && needDynamicBlock(battleId)) {
                blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_OPEN);
            }
        }
        packet.setBlockMap(stageVo.getDynamicBlockMap());
        packet.addBlockStatusMap(blockStatus);
        FamilyWarRpcHelper.roleService().send(mainServerId, roleId, packet);
        // 记住fightServerId
        // 抛事件
        FamilyWarRpcHelper.roleService().notice(mainServerId, roleId, new FamilyWarFighterAddingSucceededEvent(fightServerId, SceneManager.SCENETYPE_FAMILY_WAR_ELITE_FIGHT));
        modifyConnectorRoute(serverId, mainServerId, roleId, fightServerId);
        qualifying.onEliteFighterAddingSucceeded(mainServerId, fightServerId, battleId, fightId, roleId);
    }

    @Override
    public void onNormalFighterAddingSucceeded(int serverId, int mainServerId, int fightServerId, String battleId, String fightId, byte camp, long roleId) {
        LogUtil.info("familywar|qualifying|onNormalFighterAddingSucceeded|roleId:{},battleId:{},battleMap:{}", roleId, battleId, qualifying.getBattleMap().keySet());
        if (battleId == null) {
            long familyId = qualifying.getMemberMap().get(roleId).getFamilyId();
            battleId = qualifying.getFamilyMap().get(familyId).getBattleId();
            LogUtil.info("familywar|onFighterAddingSucceeded|a exception ,fightServer return a error battleId , get a new one:{}", battleId);
        }
        if (qualifying.getFighterReviveState(battleId, fightId, Long.toString(roleId)) == 1) return;

        StageinfoVo stageVo = SceneManager.getStageVo(stageIdOfNormalFight);
        ClientEnterPK enterPacket = new ClientEnterPK();
        enterPacket.setFightType(SceneManager.SCENETYPE_FAMILY_WAR_NORMAL_FIGHT);
        enterPacket.setStageId(stageIdOfNormalFight);
        enterPacket.setLimitTime(getNormalFightRemainTime(battleId, fightId));
        enterPacket.setCountdownOfBegin(/*timeOfNoramlFightWaiting*/0);
        enterPacket.setSkillVoMap(FamilyWarUtil.getAllRoleSkillVoMap());
        enterPacket.addMonsterVoMap(stageVo.getMonsterVoMap());
        FighterEntity fighterEntity = qualifying.getMemberMap().get(roleId).getFighterEntity();
        fighterEntity.setCamp(camp);
        if (camp == FamilyWarConst.K_CAMP1) {
            fighterEntity.setPosition(stageVo.getPosition());
            fighterEntity.setRotation(stageVo.getRotation());
        } else {
            fighterEntity.setPosition(stageVo.getEnemyPos(0));
            fighterEntity.setRotation(stageVo.getEnemyRot(0));
        }
        List<FighterEntity> list = new ArrayList<>();
        list.add(fighterEntity);
        enterPacket.setFighterEntityList(list);

        // 记住fightServerId
        // 抛事件
        FamilyWarRpcHelper.roleService().notice(mainServerId, roleId, new FamilyWarFighterAddingSucceededEvent(fightServerId, SceneManager.SCENETYPE_FAMILY_WAR_NORMAL_FIGHT));
        modifyConnectorRoute(serverId, mainServerId, roleId, fightServerId);
        qualifying.onEliteFighterAddingSucceeded(mainServerId, fightServerId, battleId, fightId, roleId);
        FamilyWarRpcHelper.roleService().send(mainServerId, roleId, enterPacket);
    }

    private int getNormalFightRemainTime(String battleId, String fightId) {
        FamilyWarKnockoutBattle battle = qualifying.getBattleMap().get(battleId);
        return battle.getNormalFightRemainTime(fightId);
    }

    @Override
    public void modifyConnectorRoute(int serverId, int mainServerId, long roleId, int fightServerId) {
        LogUtil.info("familywar|{} 跨服切换连接 from:{} to :{}", roleId, mainServerId, fightServerId);
        FamilyWarRpcHelper.familyWarService().modifyConnectorRoute(mainServerId, roleId, fightServerId);
    }

    @Override
    public void handleFighterQuit(int serverId, int mainServerId, int fightServerId, String fightId, String battleId, long roleId, short type) {
        qualifying.handleFighterQuit(battleId, roleId, fightId);
        ArrayList<String> entityKey = new ArrayList<>();
        entityKey.add(Long.toString(roleId));
        ServerOrder order = ServerOrders.newAiOrder(ServerOrder.OPEN_AI, entityKey);
        FamilyWarRpcHelper.fightBaseService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                mainServerId, fightId, order);
    }

    @Override
    public void onClientPreloadFinished(int serverId, int mainServerId, int fightServerId, String battleId, String fightId, long roleId) {
        qualifying.onClientPreloadFinished(mainServerId, battleId, fightId, roleId);
    }

    @Override
    public void handleEliteFightDamage(int serverId, int mainServerId, String battleId, String fightId, Map<String, HashMap<String, Integer>> damageMap) {
        qualifying.handleDamage(battleId, fightId, damageMap);
    }

    @Override
    public void handleEliteFightDead(int serverId, int mainServerId, String battleId, String fightId, Map<String, String> deadMap) {
        qualifying.handleDead(battleId, fightId, deadMap);
    }

    @Override
    public void handleNormalFightDamage(int serverId, int mainServerId, String battleId, String fightId, Map<String, HashMap<String, Integer>> damageMap) {
        qualifying.handleDamage(battleId, fightId, damageMap);
    }

    @Override
    public void handleNormalFightDead(int serverId, int mainServerId, String battleId, String fightId, Map<String, String> deadMap) {
        qualifying.handleDead(battleId, fightId, deadMap);
    }

    @Override
    public void handleStageFightDead(int serverId, int mainServerId, String battleId, String fightId, Map<String, String> deadMap) {
        qualifying.handleDead(battleId, fightId, deadMap);
    }

    @Override
    public void revive(int serverId, int mainServerId, int fightServerId, String battleId, String fightId, long roleId, byte reqType) {
        qualifying.revive(mainServerId, battleId, fightId, roleId, reqType);
    }

    @Override
    public void handleRevive(int serverId, String battleId, String fightId, String fighterUid) {
        qualifying.handleRevive(battleId, fightId, fighterUid);
    }

    @Override
    public void onStageFightCreationSucceeded(int serverId, int mainServerId, int fightServerId, String battleId, String fightId) {
        Map<String, FighterEntity> entityMap = qualifying.getStageFighterEntities(battleId, fightId);
        if (StringUtil.isEmpty(entityMap)) return;
        List<FighterEntity> list = new ArrayList<>();
        list.addAll(entityMap.values());
        FamilyWarRpcHelper.fightBaseService().addFighter(fightServerId, FightConst.T_FAMILY_WAR_STAGE_FIGHT, MultiServerHelper.getServerId(),
                fightId, list);
    }

    @Override
    public void onStageFighterAddingSucceeded(int serverId, int mainServerId, int fightServerId, String battleId, String fightId, Set<Long> entitySet) {
        Map<String, FighterEntity> entityMap = qualifying.getStageFighterEntities(battleId, fightId);
        if (StringUtil.isEmpty(entityMap)) return;
        FighterEntity entity;
        for (long roleId : entitySet) {
            entity = entityMap.get(Long.toString(roleId));
            if (entity == null) continue;
            qualifying.onStageFightCreateSucceeded(mainServerId, fightServerId, battleId, fightId, roleId);
            // 抛事件
            FamilyWarRpcHelper.roleService().notice(qualifying.getMainServerId(roleId), roleId, new FamilyWarFighterAddingSucceededEvent(fightServerId, SceneManager.SCENETYPE_FAMILY_WAR_STAGE_FIGTH));
            modifyConnectorRoute(serverId, qualifying.getMainServerId(roleId), roleId, fightServerId);
            ClientEnterFamilyWarEliteFight packet = new ClientEnterFamilyWarEliteFight();
            packet.setFightType(SceneManager.SCENETYPE_FAMILY_WAR_STAGE_FIGTH);
            packet.setStageId(FamilyActWarManager.stageIdOfStageFight);
            StageinfoVo stageVo = SceneManager.getStageVo(FamilyActWarManager.stageIdOfStageFight);
            packet.setLimitTime(stageVo.getFailConMap().get(SceneManager.FAIL_CONDITION_TIME) / 1000);
            entity.setPosition(stageVo.getPosition());
            entity.setRotation(stageVo.getRotation());
            LogUtil.info("familywar|position:{},rotation:{}", stageVo.getPosition(), stageVo.getRotation());
            List<FighterEntity> list = new ArrayList<>();
            list.add(entity);
            packet.setFighterEntityList(list); //
            packet.addMonsterVoMap(stageVo.getMonsterVoMap());
            FamilyWarRpcHelper.roleService().send(qualifying.getMainServerId(roleId), roleId, packet);
        }
    }

    @Override
    public void onNormalFightCreationSucceeded(int serverId, int mainServerId, int fightServerId, String battleId, String fightId) {
        qualifying.onNormalFightCreationSucceeded(mainServerId, fightServerId, battleId, fightId);
    }

    @Override
    public void onNormalFightStarted(int serverId, int mainServerId, int fightServerId, String battleId, String fightId) {
        qualifying.onNormalFightStarted(mainServerId, fightServerId, battleId, fightId);
    }

    private void syncBattleFightUpdateInfo() {
        if (qualifying != null) {
            qualifying.syncBattleFightUpdateInfo();
        }
    }

    public void checkAndEndTimeout() {
        if (qualifying != null) {
            qualifying.checkAndEndTimeout();
            checkServerConnect();
            updateRank();
        }
    }

    @Override
    public void sendAward_ResetPoints_NoticeMater(int step, long countdown) {
        qualifying.sendAward_ResetPoints_NoticeMater(step, countdown);
    }

    @Override
    public void sendPointsRankAward(int serverId, int fromServerId, boolean isElite, Map<Long, Integer> rankMap) {
        int rankId = 0;
        if (isElite) {
            rankId = rankAwardIdOfQualifyEliteFight;
        }
        if (!isElite) {
            rankId = rankAwardIdOfQualifyNormalFight;
        }
        for (Map.Entry<Long, Integer> entry : rankMap.entrySet()) {
            RankAwardVo vo = RankManager.getRankAwardVo(rankId, entry.getValue());
            if (vo != null) {
                try {
                    LogUtil.info("familywar|跨服海选个人积分发奖|roleId:{},serverId:{},rank:{},emailId:{}", entry.getKey(), fromServerId, entry.getValue(), vo.getEmail());
                    FamilyWarRpcHelper.familyWarService().sendEmailToSingle(fromServerId, entry.getKey(), vo.getEmail(), 0L, "系统", vo.getRewardMap(), entry.getValue().toString());
                } catch (Exception e) {
                    LogUtil.error("familywar|个人积分发奖失败:{},异常信息:{}", entry.getKey(), e);
                }
            }
        }
    }

    @Override
    public void sendFamilyRankAward(int serverId, int fromServerId, String familyName, int rank, Map<Long, Integer> rankAwardMap) {
        for (Map.Entry<Long, Integer> entry : rankAwardMap.entrySet()) {
            long roleId = entry.getKey();
            int voId = entry.getValue();
            FamilyWarRankAwardVo vo = familyRankAwardVoMap.get(voId);
            if (vo != null) {
                FamilyWarRpcHelper.familyWarService().sendEmailToSingle(fromServerId, roleId, vo.getTemplateId(), 0L, "系统", vo.getToolMap());
            }
        }
    }

    @Override
    public void viewMinPointsAward(int serverId, int fromServerId, long roleId) {
        if (qualifying == null) {
            ClientFamilyWarUiMinPointsAward packet = new ClientFamilyWarUiMinPointsAward(
                    ClientFamilyWarUiMinPointsAward.SUBTYPE_VIEW, FamilyWarConst.MIN_AWARD_QUALIFYING_ELITE, 0, null);
            FamilyWarRpcHelper.roleService().send(fromServerId, roleId, packet);
            return;
        }
        qualifying.viewMinPointsAward(fromServerId, roleId);
    }

    @Override
    public void acquireMinPointsAward(int serverId, int fromServerId, long roleId, long points) {
        if (qualifying == null) return;
        qualifying.acquireMinPointsAward(fromServerId, roleId, points);
    }

    @Override
    public void sendPointsRank(int serverId, int fromServerId, long roleId, byte subtype) {
        if (qualifying == null) return;
        switch (FamilyWarConst.STEP_OF_GENERAL_FLOW) {
            case FamilyWarFlow.STEP_REMOTE_QUALIFYING_START:
                qualifying.sendPointsRank(fromServerId, roleId, subtype, ClientFamilyWarUiFixtures.T_QUALIFY);
                break;
        }
    }

    @Override
    public void reqSupport(int serverId, int fromServerId, long roleId, long familyId) {
        if (qualifying == null) return;
        qualifying.reqSupport(fromServerId, roleId, familyId);
    }

    @Override
    public void addSupport(int serverId, int fromServerId, long roleId, long familyId) {
        if (qualifying == null) return;
        qualifying.addSupport(fromServerId, roleId, familyId);
    }

    @Override
    public void sendApplicationSheet(int serverId, int fromServerId, long familyId, long roleId) {
        if (FamilyWarConst.STEP_OF_GENERAL_FLOW == FamilyWarFlow.STEP_REMOTE_QUALIFYING_START) {
            if (qualifying == null) {
                FamilyWarRpcHelper.roleService().warn(fromServerId, roleId, "比赛尚未开始");
                return;
            }
            qualifying.sendApplicationSheet(fromServerId, familyId, roleId);
        }
    }

    @Override
    public void apply(int serverId, int fromServerId, long familyId, long roleId) {
        if (FamilyWarConst.STEP_OF_GENERAL_FLOW == FamilyWarFlow.STEP_REMOTE_QUALIFYING_START) {
            if (qualifying == null) {
                FamilyWarRpcHelper.roleService().warn(fromServerId, roleId, "比赛尚未开始");
                return;
            }
            qualifying.applyEliteFightSheet(fromServerId, familyId, roleId);
        }
    }

    @Override
    public void cancelApply(int serverId, int fromServerId, long familyId, long roleId) {
        if (FamilyWarConst.STEP_OF_GENERAL_FLOW == FamilyWarFlow.STEP_REMOTE_QUALIFYING_START) {
            if (qualifying == null) {
                FamilyWarRpcHelper.roleService().warn(fromServerId, roleId, "比赛尚未开始");
                return;
            }
            qualifying.cancelApplyEliteFightSheet(fromServerId, familyId, roleId);
        }
    }

    @Override
    public void confirmTeamSheet(int serverId, int fromServerId, long familyId, long roleId, Set<Long> teamSheet) {
        if (FamilyWarConst.STEP_OF_GENERAL_FLOW == FamilyWarFlow.STEP_REMOTE_QUALIFYING_START) {
            if (qualifying == null) {
                FamilyWarRpcHelper.roleService().warn(fromServerId, roleId, "比赛尚未开始");
                return;
            }
            qualifying.confirmTeamSheet(fromServerId, familyId, roleId, teamSheet);
        }
    }

    @Override
    public void sendFixtures(int serverId, int fromServerId, long familyId, long roleId, long fightScore) {
        if (qualifying != null) {
            qualifying.sendFixtures(fromServerId, familyId, roleId, fightScore);
        }
    }

    @Override
    public void sendUpdatedFixtures(int serverId, int fromServerId, long familyId, long roleId) {

    }

    @Override
    public void sendTeamSheetChangedEmail(int serverId, int mainServerId, long familyId, Set<Long> addTeamSheet, Set<Long> delTeamSheet) {
        for (Long roleId : addTeamSheet) {
            FamilyWarRpcHelper.familyWarService().sendEmailToSingle(mainServerId, roleId, emailTemplateIdOfAddingToTeamSheet, 0L, "系统", null);
        }
        for (Long roleId : delTeamSheet) {
            FamilyWarRpcHelper.familyWarService().sendEmailToSingle(mainServerId, roleId, emailTemplateIdOfDeletingFromTeamSheet, 0L, "系统", null);
        }
    }

    @Override
    public void sendFamilyRankObj(int serverId, int fromServerId, long familyId, long roleId) {
        if (qualifying == null) {
            FamilyWarRpcHelper.roleService().warn(fromServerId, roleId, "");
            return;
        }
        qualifying.sendFamilyRankObj(fromServerId, familyId, roleId);
    }

    @Override
    public void updateFixtureCache(int serverId, int battleType, int groupId, long winnerFamilyId) {
        for (FamilyWarQualifyingFixtureCache cache : fixtureCacheMap.get(battleType).get(groupId)) {
            if (isCache(cache, winnerFamilyId)) {
                LogUtil.info("familywar|updateCache battleType:{},groupId:{}, camp1:{},camp2:{},winner:{}", battleType, groupId, cache.getCamp1FamilyId(), cache.getCamp2FamilyId(), winnerFamilyId);
                cache.setWinnerFamilyId(winnerFamilyId);
                rowDao.update(cache);
            }
        }
        rowDao.flush();
    }

    @Override
    public void generateRemoteQulifications(int serverId, List<FamilyWarRemoteFamily> familyList) {
        for (FamilyWarRemoteFamily remoteFamily : familyList) {
            rowDao.insert(remoteFamily);
        }
        rowDao.flush();
    }

    @Override
    public void AsyncFihterEntityAndLockFamily(int serverId) {
        qualifying.AsyncFihterEntityAndLockFamily();
    }

    @Override
    public void updateFighterEntity(int serverId, Map<Long, FighterEntity> entityMap) {
        qualifying.updateFighterEntity(entityMap);
    }

    private boolean isCache(FamilyWarQualifyingFixtureCache cache, long winnerFamilyId) {
        return cache.getCamp1FamilyId() == winnerFamilyId || cache.getCamp2FamilyId() == winnerFamilyId;
    }

    /**
     * 是否需要动态阻挡
     *
     * @return
     */
    public boolean needDynamicBlock(String battleId) {
        int remainderTime = getDynamicBlockRemainderTime(battleId);
        return remainderTime > 0 && remainderTime <= FamilyActWarManager.DYNAMIC_BLOCK_TIME;
    }

    /**
     * 离比赛开始的剩余时间（秒）
     *
     * @return
     */
    public int getDynamicBlockRemainderTime(String battleId) {
        int qualifyingFlowStep = FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW;
        if (qualifyingFlowStep != FamilyWarQualifyingFlow.STEP_START_1ST
                && qualifyingFlowStep != FamilyWarQualifyingFlow.STEP_START_2ND
                && qualifyingFlowStep != FamilyWarQualifyingFlow.STEP_START_3RD
                && qualifyingFlowStep != FamilyWarQualifyingFlow.STEP_START_4TH
                && qualifyingFlowStep != FamilyWarQualifyingFlow.STEP_START_5TH) {
            return 0;
        }
        Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_QUALIFYING);
        if (StringUtil.isEmpty(flowMap) || !flowMap.containsKey(qualifyingFlowStep)) {
            return 0;
        }
        FamilyWarKnockoutBattle battle = qualifying.getBattleMap().get(battleId);
        long startTime = battle.getStartFightTimeStamp();
        long remainderTime = startTime + FamilyActWarManager.DYNAMIC_BLOCK_TIME * 1000 - System.currentTimeMillis();
        return (int) ((remainderTime < 0 ? 0 : remainderTime) / 1000);
    }

    /**
     * 获取怪物的战斗实体
     *
     * @param stageId
     * @return
     */
    public Map<String, FighterEntity> getMonsterFighterEntity(int stageId) {
        Map<String, FighterEntity> retMap = new HashMap<>();
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        for (int monsterSpawnId : stageVo.getMonsterSpawnIdList()) {
            retMap.putAll(spawnMonster(stageId, monsterSpawnId));
        }
        return retMap;
    }

    public Map<String, FighterEntity> spawnMonster(int stageId, int monsterSpawnId) {
        Map<String, FighterEntity> resultMap = new HashMap<>();
        MonsterSpawnVo monsterSpawnVo = SceneManager.getMonsterSpawnVo(monsterSpawnId);
        if (monsterSpawnVo == null) {
            LogUtil.error("familywar|找不到刷怪组配置monsterspawnid={},请检查表", monsterSpawnId, new IllegalArgumentException());
            return resultMap;
        }
        int index = 0;
        for (MonsterAttributeVo monsterAttrVo : monsterSpawnVo.getMonsterAttrList()) {
            String monsterUniqueId = getMonsterUId(stageId, monsterSpawnId, monsterAttrVo.getStageMonsterId());
            FighterEntity monsterEntity = FighterCreator.create(FighterEntity.TYPE_MONSTER, monsterUniqueId,
                    getSpawnUId(monsterSpawnId), monsterSpawnId, monsterAttrVo, monsterSpawnVo.getAwake(),
                    monsterSpawnVo.getSpawnDelayByIndex(index++), null);
            resultMap.put(monsterUniqueId, monsterEntity);
        }
        return resultMap;
    }

    protected String getSpawnUId(int spawnId) {
        return Integer.toString(spawnId);
    }

    protected String getMonsterUId(int stageId, int spawnId, int monsterId) {
        return "m" + stageId + getSpawnUId(spawnId) + monsterId;
    }

    /**
     * 离比赛结束的剩余时间
     *
     * @return
     */
    public int getBattleEndRemainderTime(String battleId) {
        int qualifyingFlowStep = FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW;
        if (qualifyingFlowStep != FamilyWarQualifyingFlow.STEP_START_1ST
                && qualifyingFlowStep != FamilyWarQualifyingFlow.STEP_START_2ND
                && qualifyingFlowStep != FamilyWarQualifyingFlow.STEP_START_3RD
                && qualifyingFlowStep != FamilyWarQualifyingFlow.STEP_START_4TH
                && qualifyingFlowStep != FamilyWarQualifyingFlow.STEP_START_5TH) {
            return 0;
        }
        int tempStep = qualifyingFlowStep + 1;
        Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_QUALIFYING);
        if (StringUtil.isEmpty(flowMap) || !flowMap.containsKey(tempStep)) {
            return 0;
        }
        FamilyWarKnockoutBattle battle = qualifying.getBattleMap().get(battleId);
        long startTime = battle.getStartFightTimeStamp();
        long remainderTime = startTime + FamilyActWarManager.familywar_lasttime * 1000 - System.currentTimeMillis();
        return (int) (remainderTime / 1000);
    }

    private void generateQualifying(List<KnockoutFamilyInfo> infoList, FamilyWarQualifying qualifying) {
        List<Long> familyIds = new ArrayList<>();
        for (KnockoutFamilyInfo familyInfo : infoList) {
            qualifying.addFamilyInfo(familyInfo);
            qualifying.sendMainIconToMaster(familyInfo);
            familyIds.add(familyInfo.getFamilyId());
        }
        ServiceHelper.familywarRankService().updateTitle(MultiServerHelper.getServerId(), familyIds, RankConst.HAVE_QUALIFY, RankConst.W_TYPE_QUALIFY);
    }

    private void onCallLocalService() {
        Map<Integer, List<Long>> listOfFamilyIdMap = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : familyServerId.entrySet()) {
            List<Long> listOfFamilyIds = listOfFamilyIdMap.get(entry.getValue());
            if (listOfFamilyIds == null) {
                listOfFamilyIds = new ArrayList<>();
                listOfFamilyIdMap.put(entry.getValue(), listOfFamilyIds);
            }
            listOfFamilyIds.add(entry.getKey());
        }
        for (Map.Entry<Integer, List<Long>> entry : listOfFamilyIdMap.entrySet()) {
            FamilyWarRpcHelper.familyWarService().getOnRankFamily(entry.getKey(), FamilyWarConst.W_TYPE_QUALIFYING, entry.getValue());
        }
    }

    public void generateFixture() {
        for (Map.Entry<Integer, List<Long>> entry : groupIdfamilyIdsMap.entrySet()) {
            for (int i = 0; i < FamilyWarConst.GROUP_FAMILY_COUNT - 1; i++) {
                long tmp = entry.getValue().remove(entry.getValue().size() - 1);
                entry.getValue().add(1, tmp);
                generateFixtureCache(i + 1, entry, fixtureCacheMap);
            }
        }
        rowDao.flush();
    }

    private void generateFixtureCache(int battleType, Map.Entry<Integer, List<Long>> entry, Map<Integer, Map<Integer, List<FamilyWarQualifyingFixtureCache>>> fixtureCacheMap) {//0-5,,1-4,,2-3
        for (int i = 0; i < entry.getValue().size() / 2; i++) {
            try {
                FamilyWarQualifyingFixtureCache cache = new FamilyWarQualifyingFixtureCache();
                long camp1 = entry.getValue().get(i);
                long camp2 = entry.getValue().get(entry.getValue().size() - i - 1);
                cache.setBattleType(battleType);
                cache.setGroupId(entry.getKey());
                cache.setCamp1FamilyId(camp1);
                cache.setCamp2FamilyId(camp2);
                cache.setCamp1ServerId(familyServerId.get(camp1));
                cache.setCamp2ServerId(familyServerId.get(camp2));
                cache.setMarkfinish(0L);
                Map<Integer, List<FamilyWarQualifyingFixtureCache>> cacheMap = fixtureCacheMap.get(battleType);
                if (cacheMap == null) {
                    cacheMap = new HashMap<>();
                    fixtureCacheMap.put(battleType, cacheMap);
                }
                List<FamilyWarQualifyingFixtureCache> cacheList = cacheMap.get(entry.getKey());
                if (cacheList == null) {
                    cacheList = new ArrayList<>();
                    cacheMap.put(entry.getKey(), cacheList);
                }
                cacheList.add(cache);
                generateFamilyCacheMap(cache);
                rowDao.insert(cache);
                LogUtil.info("familywar|day:{},groupId:{},camp1FamilyId:{},camp2FamilyId:{}", cache.getBattleType(),
                        cache.getGroupId(), cache.getCamp1FamilyId(), cache.getCamp2FamilyId());
            } catch (Exception e) {
                LogUtil.info("familywar|i:{},j:{}", i, entry.getValue().size() - i);
                e.printStackTrace();
            }
        }
    }

    private void generateFamilyCacheMap(FamilyWarQualifyingFixtureCache cache) {
        generateFamilyCacheMap(cache.getCamp1FamilyId(), cache);
        generateFamilyCacheMap(cache.getCamp2FamilyId(), cache);
    }

    private void generateFamilyCacheMap(long familyId, FamilyWarQualifyingFixtureCache cache) {
        List<FamilyWarQualifyingFixtureCache> cacheList = familyIdCacheList.get(familyId);
        if (cacheList == null) {
            cacheList = new ArrayList<>();
            familyIdCacheList.put(familyId, cacheList);
        }
        cacheList.add(cache);
    }

    private void loadFixtrueData() throws SQLException {
        String sql = "select * from qualifyingfixture";
        List<FamilyWarQualifyingFixtureCache> cacheLists = DBUtil.queryList(DBUtil.DB_COMMON,
                FamilyWarQualifyingFixtureCache.class, sql);
        for (FamilyWarQualifyingFixtureCache cache : cacheLists) {
            Map<Integer, List<FamilyWarQualifyingFixtureCache>> cacheMap = fixtureCacheMap.get(cache.getGroupId());
            if (cacheMap == null) {
                cacheMap = new HashMap<>();
                fixtureCacheMap.put(cache.getGroupId(), cacheMap);
            }
            List<FamilyWarQualifyingFixtureCache> cacheList = cacheMap.get(cache.getBattleType());
            if (cacheList == null) {
                cacheList = new ArrayList<>();
                cacheMap.put(cache.getBattleType(), cacheList);
            }
            cacheList.add(cache);
            generateFamilyCacheMap(cache);
        }
    }

    private void generateGroup(int familyCount, List<AbstractRankPo> familyDataList) {
        int groupSize = familyCount / FamilyWarConst.GROUP_FAMILY_COUNT;
        Map<Integer, List<Long>> groupIdfamilyIdsMap = new HashMap<>();
        Map<Long, Integer> familyIdToGroupId = new HashMap<>();
        for (int i = 0; i < groupSize; i++) {
            List<Long> familyIds = new LinkedList<>();
            for (int j = 0; j < FamilyWarConst.GROUP_FAMILY_COUNT; j++) {
                try {
                    LogUtil.info("familywar|sum:{}", i + groupSize * j);
                    long familyId = familyDataList.get(i + groupSize * j).getUniqueId();
                    familyIds.add(familyId);
                    familyIdToGroupId.put(familyId, i);
                } catch (Exception e) {
                    LogUtil.info("familywar|i:{},j:{},groupSize:{}", i, j, groupSize);
                    e.printStackTrace();
                }
            }
            groupIdfamilyIdsMap.put(i, familyIds);
        }
        this.groupIdfamilyIdsMap = new HashMap<>(groupIdfamilyIdsMap);
        this.familyIdToGroupId = new HashMap<>(familyIdToGroupId);
        List<Long> tmpList = new ArrayList<>();
        for (long familyId : familyServerId.keySet()) {
            if (!familyIdToGroupId.containsKey(familyId)) {
                tmpList.add(familyId);
            }
        }
        for (long familyId : tmpList) {
            familyServerId.remove(familyId);
        }
    }


    private int getFamilyCount() {
        int serverSize = familyWarServerMap.size();
        LogUtil.info("familywar|server:{}", familyWarServerMap);
        return FamilyActWarManager.getFamilyCount(serverSize);
    }


    @Override
    public void startQualifying(int serverId) {
        qualifying = new FamilyWarQualifying();
        LogUtil.info("familywar|server:{}", familyWarServerMap);
        for (int id : familyWarServerMap.keySet()) {
            FamilyWarRpcHelper.familyWarService().startQualifying(id);
        }
        deleteDataBase();
    }

    private void deleteDataBase() {
        try {
            LogUtil.info("familywar|每一轮海选开始时都要清掉决赛的数据库");
            DBUtil.execSql(DBUtil.DB_COMMON, "delete from familywarremotefamily");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void match(int serverId) {
        qualifying.match();
    }

    @Override
    public void AsyncState(int serverId, int battleType, int generalFlow, int subFlow, boolean isRunning) {
        FamilyWarConst.battleType = battleType;
        FamilyWarConst.STEP_OF_GENERAL_FLOW = generalFlow;
        FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW = subFlow;
        FamilyWarFlow.isMultiServerRunning = isRunning;
        LogUtil.info("familywar|状态同步 battleType:{},genFlow:{},subFlow:{},isMulti:{},serverOpenDay:{}", battleType, generalFlow, subFlow, isRunning);
    }

    @Override
    public void odd(int serverId, int odd) {
        FamilyActWarManager.familywar_pairstageodd = odd;
    }

    @Override
    public void addMember(int serverId, long familyId, FamilyMemberPo memberPo, FighterEntity entity) {
        if (qualifying == null) return;
        qualifying.addMember(familyId, memberPo, entity);
    }

    @Override
    public void delMember(int serverId, long familyId, long roleId) {
        if (qualifying == null) return;
        qualifying.delMember(familyId, roleId);
    }

    @Override
    public void roleState(int serverId, Set<Long> roleIds, boolean isOnline) {
        if (isOnline) {
            FamilyWarOnlinePlayerMap.roleOnline(roleIds);
        } else {
            FamilyWarOnlinePlayerMap.roleOffline(roleIds);
        }
    }

    @Override
    public void sendEliteFightAward(int serverId, int mainServerId, boolean isWin, int type, int count, String opponentFamilyName, Map<Long, Map<Integer, Integer>> fighterAwardMap) {
        if (isWin) {
            for (Map.Entry<Long, Map<Integer, Integer>> entry : fighterAwardMap.entrySet()) {
                FamilyWarRpcHelper.familyWarService().sendEmailToSingle(mainServerId, entry.getKey(), emailTemplateIdOfEliteFightWinAward, 0L,
                        "系统", entry.getValue(), String.valueOf(count), opponentFamilyName);
            }
        } else {
            for (Map.Entry<Long, Map<Integer, Integer>> entry : fighterAwardMap.entrySet()) {
                FamilyWarRpcHelper.familyWarService().sendEmailToSingle(mainServerId, entry.getKey(), emailTemplateIdOfEliteFightLoseAward, 0L,
                        "系统", entry.getValue(), String.valueOf(count), opponentFamilyName);
            }
        }
    }

    @Override
    public void sendNormalFightAward(int serverId, int mainServerId, Map<Long, Map<Integer, Integer>> fighterAwardMap) {
        for (Map.Entry<Long, Map<Integer, Integer>> entry : fighterAwardMap.entrySet()) {
            FamilyWarRpcHelper.familyWarService().sendEmailToSingle(mainServerId, entry.getKey(), emailTemplateIdOfNormalFightAward, 0L,
                    "系统", entry.getValue());
        }
    }

    @Override
    public void resetDataBase(int serverId) {
        try {
            DBUtil.execSql(DBUtil.DB_COMMON, "update qualifyingfixture set markfinish = " + System.currentTimeMillis() + " where markfinish = 0");
        } catch (SQLException e) {
            LogUtil.info("familywar|跨服海选数据库重置失败:{}", serverId);
            e.printStackTrace();
        }
    }

    @Override
    public void updateFlowInfo(int serverId, int warType, byte warState) {
        for (int mainServer : familyWarServerMap.keySet()) {
            FamilyWarRpcHelper.familyWarService().updateFlowInfo(mainServer, warType, warState);
        }
    }

    /**
     * 重新拉起的流程
     *
     * @param serverId
     */
    @Override
    public void startByDisaster(int serverId) {
        try {
            String sql = "select * from qualifyingfixture where markfinish = 0";
            qualifying = new FamilyWarQualifying();
            List<FamilyWarQualifyingFixtureCache> cacheList = DBUtil.queryList(DBUtil.DB_COMMON, FamilyWarQualifyingFixtureCache.class, sql);
            Map<Integer, Map<Integer, List<FamilyWarQualifyingFixtureCache>>> fixtureCacheMap = new HashMap<>();
            Map<Long, List<FamilyWarQualifyingFixtureCache>> familyIdCacheList = new HashMap<>();
            Map<Long, Integer> familyServerId = new HashMap<>();
            Map<Long, Integer> familyIdGroupId = new HashMap<>();
            Map<Integer, List<Long>> groupIdFamilyIdsMap = new HashMap<>();
            Map<Integer, Boolean> familyWarServerMap = new HashMap<>();
            for (FamilyWarQualifyingFixtureCache cache : cacheList) {
                generateFamilyCacheMap(cache.getCamp1FamilyId(), cache);
                generateFamilyCacheMap(cache.getCamp2FamilyId(), cache);
                Map<Integer, List<FamilyWarQualifyingFixtureCache>> cacheMap = fixtureCacheMap.get(cache.getBattleType());
                if (cacheMap == null) {
                    cacheMap = new HashMap<>();
                    fixtureCacheMap.put(cache.getBattleType(), cacheMap);
                }
                List<FamilyWarQualifyingFixtureCache> cacheList1 = cacheMap.get(cache.getGroupId());
                if (cacheList1 == null) {
                    cacheList1 = new ArrayList<>();
                    cacheMap.put(cache.getGroupId(), cacheList1);
                }
                cacheList1.add(cache);
                familyServerId.put(cache.getCamp1FamilyId(), cache.getCamp1ServerId());
                familyServerId.put(cache.getCamp2FamilyId(), cache.getCamp2ServerId());
                familyIdGroupId.put(cache.getCamp1FamilyId(), cache.getGroupId());
                familyIdGroupId.put(cache.getCamp2FamilyId(), cache.getGroupId());
                familyWarServerMap.put(cache.getCamp1ServerId(), true);
                familyWarServerMap.put(cache.getCamp2ServerId(), true);
            }
            for (Map.Entry<Long, Integer> entry : familyIdGroupId.entrySet()) {
                List<Long> familyIds = groupIdFamilyIdsMap.get(entry.getValue());
                if (familyIds == null) {
                    familyIds = new ArrayList<>();
                    groupIdFamilyIdsMap.put(entry.getValue(), familyIds);
                }
                familyIds.add(entry.getKey());
            }

            for (FamilyWarQualifyingFixtureCache cache : cacheList) {
                List<FamilyWarQualifyingFixtureCache> fixtureCaches1 = familyIdCacheList.get(cache.getCamp1FamilyId());
                List<FamilyWarQualifyingFixtureCache> fixtureCaches2 = familyIdCacheList.get(cache.getCamp2FamilyId());
                generateCache(familyIdCacheList, cache, cache.getCamp1FamilyId(), fixtureCaches1);
                generateCache(familyIdCacheList, cache, cache.getCamp2FamilyId(), fixtureCaches2);
            }
            this.groupIdfamilyIdsMap = groupIdFamilyIdsMap;
            this.familyIdToGroupId = familyIdGroupId;
            this.fixtureCacheMap = fixtureCacheMap;
            this.familyIdCacheList = familyIdCacheList;
            this.familyServerId = familyServerId;
            this.familyWarServerMap = familyWarServerMap;
            qualifying.newfamilyIdCacheList(this.familyIdCacheList);
            qualifying.newfamilyIdToGroupId(this.familyIdToGroupId);
            qualifying.newGroupIdFamilyIdsMap(this.groupIdfamilyIdsMap);
            qualifying.newfixtureCacheMap(this.fixtureCacheMap);
            onCallLocalService();
            updateIconText(FamilyWarUtil.getFamilyWarServerId(), "");
            checkTimeStamp = System.currentTimeMillis();
            this.cacheList = cacheList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateRank() {
        if (checkTimeStamp == 0L) return;
        if ((System.currentTimeMillis() - checkTimeStamp) / 1000 > 900) {
            for (FamilyWarQualifyingFixtureCache cache : cacheList) {
                if (cache.getWinnerFamilyId() != 0L) {
                    qualifying.updateFamilyPointRankObj(cache.getWinnerFamilyId(), true);
                    qualifying.updateFamilyPointRankObj(getOpponentFamilyId(cache, cache.getWinnerFamilyId()), false);
                }
            }
            checkTimeStamp = 0L;
        }
    }

    private long getOpponentFamilyId(FamilyWarQualifyingFixtureCache cache, long familyId) {
        if (familyId == cache.getCamp1FamilyId()) {
            return cache.getCamp2FamilyId();
        } else {
            return cache.getCamp1FamilyId();
        }
    }

    private void generateCache(Map<Long, List<FamilyWarQualifyingFixtureCache>> familyIdCacheList,
                               FamilyWarQualifyingFixtureCache cache, long familyId, List<FamilyWarQualifyingFixtureCache> fixtureCaches) {
        if (fixtureCaches == null) {
            fixtureCaches = new ArrayList<>();
            familyIdCacheList.put(familyId, fixtureCaches);
        }
        fixtureCaches.add(cache);
    }
}
