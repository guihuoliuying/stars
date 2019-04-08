package com.stars.multiserver.familywar;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.activityflow.ActivityFlow;
import com.stars.core.activityflow.ActivityFlowUtil;
import com.stars.core.dao.DbRowDao;
import com.stars.coreManager.ExcutorKey;
import com.stars.coreManager.SchedulerManager;
import com.stars.db.DBUtil;
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
import com.stars.multiserver.familywar.flow.FamilyWarRemoteFlow;
import com.stars.multiserver.familywar.knockout.FamilyWarKnockoutBattle;
import com.stars.multiserver.familywar.knockout.KnockoutFamilyInfo;
import com.stars.multiserver.familywar.rank.RankConst;
import com.stars.multiserver.familywar.remote.FamilyWarRemote;
import com.stars.multiserver.familywar.remote.FamilyWarRemoteFamily;
import com.stars.multiserver.fight.ServerOrders;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.activities.ActConst;
import com.stars.services.chat.ChatManager;
import com.stars.services.family.FamilyConst;
import com.stars.services.family.main.userdata.FamilyMemberPo;
import com.stars.services.rank.prodata.RankAwardVo;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.util.TimeUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.stars.modules.familyactivities.war.FamilyActWarManager.*;
import static com.stars.multiserver.familywar.FamilyWarConst.*;

/**
 * Created by zhaowenshuo on 2016/12/5.
 */
public class FamilyWarRemoteServiceActor extends ServiceActor implements FamilyWarRemoteService {

    private FamilyWarRemote remote;
    private Map<Integer, Boolean> readyServerMap;
    private Map<Integer, Boolean> allCanRemoteServerMap;
    private DbRowDao rowDao;
    private FamilyWarRemoteFlow remoteFlow;
    private Map<Long, FamilyWarRemoteFamily> remoteFamilyMap;
    private Map<Integer, List<Long>> serverFamilyMap;
    private long startTimestamp = 0L;
    private int check = 0;
    private boolean initialization = false;
    private boolean isOver = false;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.FamilyWarRemoteService, this);
        rowDao = new DbRowDao(SConst.FamilyWarRemoteService, DBUtil.DB_COMMON);
        readyServerMap = new HashMap<>();
        allCanRemoteServerMap = new HashMap<>();
        remoteFlow = new FamilyWarRemoteFlow();
        remoteFlow.init(SchedulerHelper.getScheduler(), DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_REMOTE));
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.FamilyWar, new Runnable() {
            @Override
            public void run() {
                syncBattleFightUpdateInfo();
            }
        }, 20, 1, TimeUnit.SECONDS);
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.FamilyWar, new Runnable() {
            @Override
            public void run() {
                ServiceHelper.familyWarRemoteService().checkAndEndTimeout();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void printState() {

    }

    @Override
    public void generateFamilyData(int serverId, int fromServerId, List<AbstractRankPo> familyList) {

    }

    @Override
    public void registerFamilyWarServer(int serverId, int comFrom) {
        allCanRemoteServerMap.put(comFrom, false);
    }

    @Override
    public void onGenerateFinish(int serverId, int battleType, Map<Integer, List<Long>> groupOfFamilyMap,
                                 LinkedHashMap<Integer, Long> indexFamilyId, Set<Long> outOfwarFamily) {
        LogUtil.info("familywar|battleType:{},indexFamilyId:{},outOfWarFmaily:{},server:{}", battleType, indexFamilyId, outOfwarFamily, allCanRemoteServerMap.keySet());
        remoteFlow.setBattleType(battleType);
        Map<Long, List<Integer>> familyIndexs = new HashMap<>();
        if (indexFamilyId != null) {
            for (Map.Entry<Integer, Long> entry : indexFamilyId.entrySet()) {
                List<Integer> indexs = familyIndexs.get(entry.getValue());
                if (indexs == null) {
                    indexs = new ArrayList<>();
                    familyIndexs.put(entry.getValue(), indexs);
                }
                indexs.add(entry.getKey());
            }
        }
        for (List<Long> familyIdList : groupOfFamilyMap.values()) {
            for (long familyId : familyIdList) {
                FamilyWarRemoteFamily remoteFamily = remoteFamilyMap.get(familyId);
                if (remoteFamily == null) continue;
                remoteFamily.setBattleType(battleType);
                if (battleType != R_BATTLE_TYPE_4TO2 && battleType != R_BATTLE_TYPE_FINAL) {
                    remoteFamily.setRank(battleType);
                }
                if (indexFamilyId != null) {
                    for (int index : familyIndexs.get(familyId)) {
                        remoteFamily.addIndex(index);
                    }
                }
                rowDao.update(remoteFamily);
            }
        }
        for (int id : allCanRemoteServerMap.keySet()) {
            LogUtil.info("familywar|同步battleType:{},到 {} 服", battleType, allCanRemoteServerMap.keySet());
            FamilyWarRpcHelper.familyWarService().AsyncBattle(id, battleType);
        }
        rowDao.flush();
    }


    @Override
    public void update_1stTO_4thFamily(int serverId, long _1st, long _2nd, long _3rd, long _4th) {
        FamilyWarRemoteFamily _1Family = remoteFamilyMap.get(_1st);
        _1Family.setRank(1);
        rowDao.update(_1Family);
        FamilyWarRemoteFamily _2Family = remoteFamilyMap.get(_2nd);
        _2Family.setRank(2);
        rowDao.update(_2Family);
        FamilyWarRemoteFamily _3Family = remoteFamilyMap.get(_3rd);
        _3Family.setRank(3);
        rowDao.update(_3Family);
        FamilyWarRemoteFamily _4Family = remoteFamilyMap.get(_4th);
        _4Family.setRank(4);
        rowDao.update(_4Family);
        rowDao.flush();
    }

    @Override
    public void SyncBattleType(int serverId, int battleType) {
        remoteFlow.setBattleType(battleType);
        if (battleType == R_BATTLE_TYPE_OVER) {
            isOver = true;
        }
    }

    @Override
    public void onRankFamilyData(int serverId, int fromServerId, List<KnockoutFamilyInfo> infoList) {
        if (readyServerMap.get(fromServerId)) {
            LogUtil.info("familywar|家族数据重复了:{}", fromServerId);
            return;
        }
        if (initialization)
            return;
        readyServerMap.put(fromServerId, true);
        generateRemote(infoList, remote);
        if (isAllReady()) {
            doAllServerConnect();
        }
    }

    private void doAllServerConnect() {
        initialization = true;
        remote.generateFixture();
    }

    private void checkServerConnect() {
        if (isAllReady())
            return;
        check++;
        if ((System.currentTimeMillis() - startTimestamp) / 1000 > 60) {
            if (check >= 60) {
                List<Integer> unConnectServerList = new ArrayList<>();
                for (Map.Entry<Integer, Boolean> entry : readyServerMap.entrySet()) {
                    if (!entry.getValue()) {
                        unConnectServerList.add(entry.getKey());
                    }
                }
                LogUtil.info("familywar|距离取资格已经过去 {} 秒，尝试重新连接 :{} 服务的家族数据", (System.currentTimeMillis() - startTimestamp) / 1000, unConnectServerList);
                for (int id : unConnectServerList) {
                    FamilyWarRpcHelper.familyWarService().getOnRankFamily(id, FamilyWarConst.W_TYPE_REMOTE, serverFamilyMap.get(id));
                }
                check = 0;
            }
        }
        if ((System.currentTimeMillis() - startTimestamp) / 1000 > 1200 && !initialization) {
            LogUtil.info("familywar|超时触发家族战跨服决赛流程 readyServerMap:{}", readyServerMap);
            doAllServerConnect();
        }
    }

    private boolean isAllReady() {
        for (Boolean aBoolean : readyServerMap.values()) {
            if (!aBoolean)
                return false;
        }
        return true;
    }

    private void generateRemote(List<KnockoutFamilyInfo> infoList, FamilyWarRemote remote) {
        List<Long> familyIds = new ArrayList<>();
        for (KnockoutFamilyInfo familyInfo : infoList) {
            remote.addFamilyInfo(familyInfo);
            remote.sendMainIconToMaster(familyInfo);
            remote.initRoleTips(familyInfo);
            familyIds.add(familyInfo.getFamilyId());
        }
        ServiceHelper.familywarRankService().updateTitle(MultiServerHelper.getServerId(), familyIds, RankConst.HAVE_REMOTE, RankConst.W_TYPE_REMOTE);
    }

    private void syncBattleFightUpdateInfo() {
        if (remote != null) {
            remote.syncBattleFightUpdateInfo();
        }
    }

    public void checkAndEndTimeout() {
        if (remote != null) {
            remote.checkAndEndTimeout();
            checkServerConnect();
        }
    }

    @Override
    public void startBattle(int serverId, int battleType) {
        remote.startBattle(battleType);
    }

    @Override
    public void endBattle(int serverId, int battleType) {
        remote.endBattle(battleType);
    }

    @Override
    public void match(int serverId) {
        remote.match();
    }

    @Override
    public void updateIconText(int serverId, String text) {
        LogUtil.info("familywar|发送活动icon文本text:{}", text);
        updateIconText(text);
    }

    private void updateIconText(String text) {
        if (remote == null) {
            for (int id : allCanRemoteServerMap.keySet()) {
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
            return;
        }
        remote.updateIconText(text, allCanRemoteServerMap);
    }

    @Override
    public void enterSafeScene(int serverId, int fromServerId, long roleId) {
        LogUtil.info("familywar|进入备战场景|总赛程阶段:{}|子赛程阶段:{}", FamilyWarConst.STEP_OF_GENERAL_FLOW, FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW);
        if (FamilyWarConst.STEP_OF_GENERAL_FLOW == FamilyWarFlow.STEP_REMOTE_KNOCKOUT_START) {
            remote.enterSafeScene(serverId, fromServerId, roleId);
        }
    }

    @Override
    public void enterSafeScene(int serverId, int fromServerId, long familyId, long roleId) {
        LogUtil.info("familywar|进入备战场景|总赛程阶段:{}|子赛程阶段:{}", FamilyWarConst.STEP_OF_GENERAL_FLOW, FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW);
        if (FamilyWarConst.STEP_OF_GENERAL_FLOW == FamilyWarFlow.STEP_REMOTE_KNOCKOUT_START) {
            remote.enterSafeScene(serverId, fromServerId, familyId, roleId);
        }
    }

    @Override
    public void enterFight(int serverId, int fromServerId, long familyId, long roleId, FighterEntity fighterEntity) {
        LogUtil.info("familywar|进入战斗场景|总赛程阶段:{}|子赛程阶段:{}", FamilyWarConst.STEP_OF_GENERAL_FLOW, FamilyWarConst.STEP_OF_SUB_QUALIFYING_FLOW);
        if (FamilyWarConst.STEP_OF_GENERAL_FLOW == FamilyWarFlow.STEP_REMOTE_KNOCKOUT_START) {
            LogUtil.info("familywar|serverActor==家族:{}的玩家:{}进入战场", familyId, roleId);
            remote.enter(serverId, fromServerId, familyId, roleId, fighterEntity);
        }
    }

    @Override
    public void cancelFight(int serverId, int fromServerId, long familyId, long roleId) {
        if (FamilyWarConst.STEP_OF_GENERAL_FLOW == FamilyWarFlow.STEP_REMOTE_KNOCKOUT_START) {
            remote.cancelNormalFightWaitingQueue(serverId, fromServerId, familyId, roleId);
        }
    }

    @Override
    public void sendMainIcon2All(int serverId, long countdown) {
        int iconState = FamilyWarUtil.getMainIconStateByRemoteFlowStep();
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
        for (int serverId : allCanRemoteServerMap.keySet()) {
            FamilyWarRpcHelper.familyWarService().sendMainIcon(serverId, mainIcon, new HashSet<>(remote.getFamilyMap().keySet()), remote.getFailFamilySet());
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
        mainIcon.setQualification(remote.isKnockoutFamily(familyId) ? FamilyWarConst.WITH_QUALIFICATION : FamilyWarConst.WITHOUT_QUALIFICATION);
        FamilyWarRpcHelper.roleService().send(mainServer, roleId, mainIcon);
    }

    @Override
    public void sendMainIcon2Role(int serverId, int fromServerId, long roleId, boolean isMaster, long familyId) {
        if (isOver)
            return;
        int iconState = FamilyWarUtil.getMainIconStateByRemoteFlowStep();
        if (iconState == ActivityFlow.STEP_START_CHECK) return;
        long countdown = 0;
        if (iconState == FamilyWarConst.STATE_START) {
            Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_REMOTE);
            if (StringUtil.isEmpty(flowMap) || !flowMap.containsKey(FamilyWarRemoteFlow.STEP_START_32TO16))
                return;
            countdown = ActivityFlowUtil.remainder(System.currentTimeMillis(), flowMap.get(FamilyWarRemoteFlow.STEP_START_32TO16));
        }
        if (iconState != FamilyWarConst.STATE_NOTICE_MASTER) {
            sendMainIcon(fromServerId, roleId, iconState, countdown, familyId);
        }
        if (isMaster && (iconState == FamilyWarConst.STATE_ICON_DISAPPEAR || iconState == FamilyWarConst.STATE_NOTICE_MASTER)) {
            remote.sendMainIconToMaster(FamilyWarConst.W_TYPE_REMOTE, familyId);
        }
    }

    @Override
    public void onPremittedToEnter(int mainServerId, int fightServerId, String fightId, byte camp, long roleId) {
        StageinfoVo stageVo = SceneManager.getStageVo(FamilyActWarManager.stageIdOfEliteFight);
        FighterEntity fighterEntity = remote.getMemberMap().get(roleId).getFighterEntity();
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
        LogUtil.info("添加玩家 :{} 成功", roleId);
        FamilyWarRpcHelper.fightBaseService().addFighter(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                MultiServerHelper.getServerId(), fightId, list);
        FamilyWarRpcHelper.fightBaseService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                MultiServerHelper.getServerId(), fightId, order);
    }

    @Override
    public void onFighterAddingSucceeded(int serverId, int mainServerId, int fightServerId, String battleId, String fightId, byte camp, long roleId) {
        if (remote.getFighterReviveState(battleId, fightId, Long.toString(roleId)) == 1) return;
        // 发进入包
        ClientEnterFamilyWarEliteFight packet = new ClientEnterFamilyWarEliteFight();
        packet.setFightType(SceneManager.SCENETYPE_FAMILY_WAR_ELITE_FIGHT);
        packet.setStageId(FamilyActWarManager.stageIdOfEliteFight);
        packet.setLimitTime(getBattleEndRemainderTime(battleId));
        packet.setStartRemainderTime(getDynamicBlockRemainderTime(battleId));
        StageinfoVo stageVo = SceneManager.getStageVo(FamilyActWarManager.stageIdOfEliteFight);
        FighterEntity fighterEntity = remote.getMemberMap().get(roleId).getFighterEntity();
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
        LogUtil.info("动态阻挡数据Actor:{}", blockStatus);
        packet.setBlockMap(stageVo.getDynamicBlockMap());
        packet.addBlockStatusMap(blockStatus);
        FamilyWarRpcHelper.roleService().send(mainServerId, roleId, packet);
        // 记住fightServerId
        // 抛事件
        FamilyWarRpcHelper.roleService().notice(mainServerId, roleId, new FamilyWarFighterAddingSucceededEvent(fightServerId, SceneManager.SCENETYPE_FAMILY_WAR_ELITE_FIGHT));
        modifyConnectorRoute(serverId, mainServerId, roleId, fightServerId);
        remote.onEliteFighterAddingSucceeded(mainServerId, fightServerId, battleId, fightId, roleId);
    }

    @Override
    public void onNormalFighterAddingSucceeded(int serverId, int mainServerId, int fightServerId, String battleId, String fightId, byte camp, long roleId) {
        LogUtil.info("familywar|remote|onNormalFighterAddingSucceeded|roleId:{},battleId:{},battleMap:{}", roleId, battleId, remote.getBattleMap().keySet());
        if (battleId == null) {
            long familyId = remote.getMemberMap().get(roleId).getFamilyId();
            battleId = remote.getFamilyMap().get(familyId).getBattleId();
            LogUtil.info("familywar|onFighterAddingSucceeded|a exception ,fightServer return a error battleId , get a new one:{}", battleId);
        }
        if (remote.getFighterReviveState(battleId, fightId, Long.toString(roleId)) == 1) return;

        StageinfoVo stageVo = SceneManager.getStageVo(stageIdOfNormalFight);
        ClientEnterPK enterPacket = new ClientEnterPK();
        enterPacket.setFightType(SceneManager.SCENETYPE_FAMILY_WAR_NORMAL_FIGHT);
        enterPacket.setStageId(stageIdOfNormalFight);
        enterPacket.setLimitTime(getNormalFightRemainTime(battleId, fightId));
        enterPacket.setCountdownOfBegin(/*timeOfNoramlFightWaiting*/0);
        enterPacket.setSkillVoMap(FamilyWarUtil.getAllRoleSkillVoMap());
        enterPacket.addMonsterVoMap(stageVo.getMonsterVoMap());
        FighterEntity fighterEntity = remote.getMemberMap().get(roleId).getFighterEntity();
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
        remote.onEliteFighterAddingSucceeded(mainServerId, fightServerId, battleId, fightId, roleId);
        FamilyWarRpcHelper.roleService().send(mainServerId, roleId, enterPacket);
    }

    private int getNormalFightRemainTime(String battleId, String fightId) {
        FamilyWarKnockoutBattle battle = remote.getBattleMap().get(battleId);
        return battle.getNormalFightRemainTime(fightId);
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
     * 离比赛结束的剩余时间
     *
     * @return
     */
    public int getBattleEndRemainderTime(String battleId) {
        int qualifyingFlowStep = FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW;
        LogUtil.info("familywar|step:{}", qualifyingFlowStep);
        if (qualifyingFlowStep != FamilyWarRemoteFlow.STEP_START_32TO16
                && qualifyingFlowStep != FamilyWarRemoteFlow.STEP_START_16TO8
                && qualifyingFlowStep != FamilyWarRemoteFlow.STEP_START_8TO4
                && qualifyingFlowStep != FamilyWarRemoteFlow.STEP_START_4TO2
                && qualifyingFlowStep != FamilyWarRemoteFlow.STEP_START_FINNAL) {
            return 0;
        }
        int tempStep = qualifyingFlowStep + 1;
        Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_REMOTE);
        if (StringUtil.isEmpty(flowMap) || !flowMap.containsKey(tempStep)) {
            return 0;
        }
        FamilyWarKnockoutBattle battle = remote.getBattleMap().get(battleId);
        long startTime = battle.getStartFightTimeStamp();
        long remainderTime = startTime + FamilyActWarManager.familywar_lasttime * 1000 - System.currentTimeMillis();
        LogUtil.info("familywar|离比赛结束时间:{},step:{}", remainderTime, qualifyingFlowStep);
        return (int) (remainderTime / 1000);
    }

    /**
     * 离比赛开始的剩余时间（秒）
     *
     * @return
     */
    public int getDynamicBlockRemainderTime(String battleId) {
        int qualifyingFlowStep = FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW;
        if (qualifyingFlowStep != FamilyWarRemoteFlow.STEP_START_32TO16
                && qualifyingFlowStep != FamilyWarRemoteFlow.STEP_START_16TO8
                && qualifyingFlowStep != FamilyWarRemoteFlow.STEP_START_8TO4
                && qualifyingFlowStep != FamilyWarRemoteFlow.STEP_START_4TO2
                && qualifyingFlowStep != FamilyWarRemoteFlow.STEP_START_FINNAL) {
            return 0;
        }
        Map<Integer, String> flowMap = DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_WAR_REMOTE);
        if (StringUtil.isEmpty(flowMap) || !flowMap.containsKey(qualifyingFlowStep)) {
            return 0;
        }
        FamilyWarKnockoutBattle battle = remote.getBattleMap().get(battleId);
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

    @Override
    public void modifyConnectorRoute(int serverId, int mainServerId, long roleId, int fightServerId) {
        LogUtil.info("familywar|{} 跨服切换连接 from:{} to :{}", roleId, serverId, fightServerId);
        FamilyWarRpcHelper.familyWarService().modifyConnectorRoute(mainServerId, roleId, fightServerId);
    }

    @Override
    public void handleFighterQuit(int serverId, int mainServerId, int fightServerId, String fightId, String battleId, long roleId, short type) {
        remote.handleFighterQuit(battleId, roleId, fightId);
        ArrayList<String> entityKey = new ArrayList<>();
        entityKey.add(Long.toString(roleId));
        ServerOrder order = ServerOrders.newAiOrder(ServerOrder.OPEN_AI, entityKey);
        FamilyWarRpcHelper.fightBaseService().addServerOrder(fightServerId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                mainServerId, fightId, order);
    }

    @Override
    public void onClientPreloadFinished(int serverId, int mainServerId, int fightServerId, String battleId, String fightId, long roleId) {
        remote.onClientPreloadFinished(mainServerId, battleId, fightId, roleId);
    }

    @Override
    public void handleEliteFightDamage(int serverId, int mainServerId, String battleId, String fightId, Map<String, HashMap<String, Integer>> damageMap) {
        remote.handleDamage(battleId, fightId, damageMap);
    }

    @Override
    public void handleEliteFightDead(int serverId, int mainServerId, String battleId, String fightId, Map<String, String> deadMap) {
        remote.handleDead(battleId, fightId, deadMap);
    }

    @Override
    public void handleNormalFightDamage(int serverId, int mainServerId, String battleId, String fightId, Map<String, HashMap<String, Integer>> damageMap) {
        remote.handleDamage(battleId, fightId, damageMap);
    }

    @Override
    public void handleNormalFightDead(int serverId, int mainServerId, String battleId, String fightId, Map<String, String> deadMap) {
        remote.handleDead(battleId, fightId, deadMap);
    }

    @Override
    public void handleStageFightDead(int serverId, int mainServerId, String battleId, String fightId, Map<String, String> deadMap) {
        remote.handleDead(battleId, fightId, deadMap);
    }

    @Override
    public void revive(int serverId, int mainServerId, int fightServerId, String battleId, String fightId, long roleId, byte reqType) {
        remote.revive(mainServerId, battleId, fightId, roleId, reqType);
    }

    @Override
    public void handleRevive(int serverId, String battleId, String fightId, String fighterUid) {
        remote.handleRevive(battleId, fightId, fighterUid);
    }

    @Override
    public void onStageFightCreationSucceeded(int serverId, int mainServerId, int fightServerId, String battleId, String fightId) {
        Map<String, FighterEntity> entityMap = remote.getStageFighterEntities(battleId, fightId);
        LogUtil.info("familywar|回调到家族战服，战斗创建成功 fightServerId:{},fightId:{},battleId:{},entityKey", fightServerId, fightId, battleId, entityMap != null ? entityMap.keySet() : "null");
        if (StringUtil.isEmpty(entityMap)) return;
        List<FighterEntity> list = new ArrayList<>();
        list.addAll(entityMap.values());
        FamilyWarRpcHelper.fightBaseService().addFighter(fightServerId, FightConst.T_FAMILY_WAR_STAGE_FIGHT, MultiServerHelper.getServerId(),
                fightId, list);
    }

    @Override
    public void onStageFighterAddingSucceeded(int serverId, int mainServerId, int fightServerId, String battleId, String fightId, Set<Long> entitySet) {
        Map<String, FighterEntity> entityMap = remote.getStageFighterEntities(battleId, fightId);
        LogUtil.info("familywar|回调到家族战服，成功添加玩家 fightServerId:{},fightId:{},battleId:{},entityKey", fightServerId, fightId, battleId, entityMap != null ? entityMap.keySet() : "null");
        if (StringUtil.isEmpty(entityMap)) return;
        FighterEntity entity;
        for (long roleId : entitySet) {
            entity = entityMap.get(Long.toString(roleId));
            if (entity == null) continue;
            remote.onStageFightCreateSucceeded(mainServerId, fightServerId, battleId, fightId, roleId);
            // 抛事件
            FamilyWarRpcHelper.roleService().notice(mainServerId, roleId, new FamilyWarFighterAddingSucceededEvent(fightServerId, SceneManager.SCENETYPE_FAMILY_WAR_STAGE_FIGTH));
            modifyConnectorRoute(FamilyWarUtil.getFamilyWarServerId(), mainServerId, roleId, fightServerId);
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
            FamilyWarRpcHelper.roleService().send(mainServerId, roleId, packet);
        }
    }

    @Override
    public void onNormalFightCreationSucceeded(int serverId, int mainServerId, int fightServerId, String battleId, String fightId) {
        remote.onNormalFightCreationSucceeded(mainServerId, fightServerId, battleId, fightId);
    }

    @Override
    public void onNormalFightStarted(int serverId, int mainServerId, int fightServerId, String battleId, String fightId) {
        remote.onNormalFightStarted(mainServerId, fightServerId, battleId, fightId);
    }

    @Override
    public void sendAward_ResetPoints_NoticeMater(int step, long countdown) {
        remote.sendAward_ResetPoints_NoticeMater(step, countdown);
    }

    @Override
    public void sendPointsRankAward(int serverId, int mainServerId, boolean isElite, Map<Long, Integer> rankMap) {
        int rankId = 0;
        if (isElite) {
            rankId = rankAwardIdOfRemoteEliteFight;
        }
        if (!isElite) {
            rankId = rankAwardIdOfRemoteNormalFight;
        }
        for (Map.Entry<Long, Integer> entry : rankMap.entrySet()) {
            RankAwardVo vo = RankManager.getRankAwardVo(rankId, entry.getValue());
            if (vo != null) {
                try {
                    LogUtil.info("familywar|跨服决赛个人积分发奖|roleId:{},serverId:{},rank:{},emailId:{}", entry.getKey(), mainServerId, entry.getValue(), vo.getEmail());
                    FamilyWarRpcHelper.familyWarService().sendEmailToSingle(mainServerId, entry.getKey(), vo.getEmail(), 0L, "系统", vo.getRewardMap(), entry.getValue().toString());
                } catch (Exception e) {
                    LogUtil.error("familywar|个人积分发奖失败:{},异常信息:{}", entry.getKey(), e);
                }
            }
        }
    }

    @Override
    public void sendFamilyRankAward(int serverId, int mainServerId, String familyName, int rank, Map<Long, Integer> rankAwardMap) {
        for (Map.Entry<Long, Integer> entry : rankAwardMap.entrySet()) {
            long roleId = entry.getKey();
            int voId = entry.getValue();
            FamilyWarRankAwardVo vo = familyRankAwardVoMap.get(voId);
            if (vo != null) {
                FamilyWarRpcHelper.familyWarService().sendEmailToSingle(mainServerId, roleId, vo.getTemplateId(), 0L, "系统", vo.getToolMap());
            }
        }
    }

    @Override
    public void viewMinPointsAward(int serverId, int fromServerId, long roleId) {
        if (remote == null) {
            ClientFamilyWarUiMinPointsAward packet = new ClientFamilyWarUiMinPointsAward(
                    ClientFamilyWarUiMinPointsAward.SUBTYPE_VIEW, FamilyWarConst.MIN_AWARD_REMOTE_ELITE, 0, null);
            FamilyWarRpcHelper.roleService().send(fromServerId, roleId, packet);
            return;
        }
        remote.viewMinPointsAward(fromServerId, roleId);
    }

    @Override
    public void acquireMinPointsAward(int serverId, int fromServerId, long roleId, long points) {
        remote.acquireMinPointsAward(fromServerId, roleId, points);
    }

    @Override
    public void sendPointsRank(int serverId, int fromServerId, long roleId, byte subtype) {
        if (remote == null) return;
        switch (FamilyWarConst.STEP_OF_GENERAL_FLOW) {
            case FamilyWarFlow.STEP_REMOTE_KNOCKOUT_START:
                remote.sendPointsRank(fromServerId, roleId, subtype, ClientFamilyWarUiFixtures.T_REMOTE);
                break;
        }
    }

    @Override
    public void reqSupport(int serverId, int fromServerId, long roleId, long familyId) {
        if (remote == null) return;
        remote.reqSupport(fromServerId, roleId, familyId);
    }

    @Override
    public void addSupport(int serverId, int fromServerId, long roleId, long familyId) {
        if (remote == null) return;
        remote.addSupport(fromServerId, roleId, familyId);
    }

    @Override
    public void sendApplicationSheet(int serverId, int fromServerId, long familyId, long roleId) {
        if (FamilyWarConst.STEP_OF_GENERAL_FLOW == FamilyWarFlow.STEP_REMOTE_KNOCKOUT_START) {
            if (remote == null) {
                FamilyWarRpcHelper.roleService().warn(fromServerId, roleId, "比赛尚未开始");
                return;
            }
            remote.sendApplicationSheet(fromServerId, familyId, roleId);
        }
    }

    @Override
    public void apply(int serverId, int fromServerId, long familyId, long roleId) {
        if (FamilyWarConst.STEP_OF_GENERAL_FLOW == FamilyWarFlow.STEP_REMOTE_KNOCKOUT_START) {
            if (remote == null) {
                FamilyWarRpcHelper.roleService().warn(MultiServerHelper.getServerId(), roleId, "比赛尚未开始");
                return;
            }
            remote.applyEliteFightSheet(fromServerId, familyId, roleId);
        }
    }

    @Override
    public void cancelApply(int serverId, int fromServerId, long familyId, long roleId) {
        if (FamilyWarConst.STEP_OF_GENERAL_FLOW == FamilyWarFlow.STEP_REMOTE_KNOCKOUT_START) {
            if (remote == null) {
                FamilyWarRpcHelper.roleService().warn(MultiServerHelper.getServerId(), roleId, "比赛尚未开始");
                return;
            }
            remote.cancelApplyEliteFightSheet(fromServerId, familyId, roleId);
        }
    }

    @Override
    public void confirmTeamSheet(int serverId, int fromServerId, long familyId, long roleId, Set<Long> teamSheet) {
        if (FamilyWarConst.STEP_OF_GENERAL_FLOW == FamilyWarFlow.STEP_REMOTE_KNOCKOUT_START) {
            if (remote == null) {
                FamilyWarRpcHelper.roleService().warn(MultiServerHelper.getServerId(), roleId, "比赛尚未开始");
                return;
            }
            remote.confirmTeamSheet(fromServerId, familyId, roleId, teamSheet);
        }
    }

    @Override
    public void sendFixtures(int serverId, int fromServerId, long familyId, long roleId) {
        if (remote != null) {
            remote.sendFixtures(fromServerId, familyId, roleId);
        }
    }

    @Override
    public void sendUpdatedFixtures(int serverId, int fromServerId, long familyId, long roleId) {

    }

    @Override
    public void AsyncFihterEntityAndLockFamily(int serverId) {
        remote.AsyncFihterEntityAndLockFamily();
    }

    @Override
    public void updateFighterEntity(int serverId, Map<Long, FighterEntity> entityMap) {
        remote.updateFighterEntity(entityMap);
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
    public void AsyncState(int serverId, int battleType, int generalFlow, int subFlow, boolean isRunning) {
        FamilyWarConst.battleType = battleType;
        FamilyWarConst.STEP_OF_GENERAL_FLOW = generalFlow;
        FamilyWarConst.STEP_OF_SUB_REMOTE_FLOW = subFlow;
        FamilyWarFlow.isMultiServerRunning = isRunning;
        LogUtil.info("familywar|状态同步 battleType:{},genFlow:{},subFlow:{},isMulti:{},serverOpenDay:{}", battleType, generalFlow, subFlow, isRunning);
    }

    @Override
    public void addMember(int serverId, long familyId, FamilyMemberPo memberPo, FighterEntity entity) {
        if (remote == null) return;
        remote.addMember(familyId, memberPo, entity);
    }

    @Override
    public void delMember(int serverId, long familyId, long roleId) {
        if (remote == null) return;
        remote.delMember(familyId, roleId);
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
    public void updateFlowInfo(int serverId, int warType, byte warState) {
        LogUtil.info("familywar|同步报名按钮到各个游戏服 warType:{},warState:{},server:{}", warType, warState, allCanRemoteServerMap.keySet());
        for (int mainServerId : allCanRemoteServerMap.keySet()) {
            FamilyWarRpcHelper.familyWarService().updateFlowInfo(mainServerId, warType, warState);
        }
    }

    @Override
    public void chat(int serverId, String message) {
        for (int id : allCanRemoteServerMap.keySet()) {
            FamilyWarRpcHelper.familyWarService().chat(id, "系统", ChatManager.CHANNEL_SYSTEM, 0L, 0L, message, false);
            FamilyWarRpcHelper.familyWarService().announce(id, message);
        }
    }

    @Override
    public void startRemote(int serverId) {
        String sql = "select * from familywarremotefamily";
        try {
            remote = new FamilyWarRemote();
            startTimestamp = System.currentTimeMillis();
            List<FamilyWarRemoteFamily> remoteFamilies = DBUtil.queryList(DBUtil.DB_COMMON, FamilyWarRemoteFamily.class, sql);
            Map<Integer, List<Long>> serverFamilyMap = new HashMap<>();
            Map<Long, FamilyWarRemoteFamily> familyMap = new HashMap<>();
            for (FamilyWarRemoteFamily remoteFamily : remoteFamilies) {
                List<Long> familyIds = serverFamilyMap.get(remoteFamily.getServerId());
                if (familyIds == null) {
                    familyIds = new ArrayList<>();
                    serverFamilyMap.put(remoteFamily.getServerId(), familyIds);
                }
                familyIds.add(remoteFamily.getFamilyId());
                familyMap.put(remoteFamily.getFamilyId(), remoteFamily);
            }
            remoteFamilyMap = new HashMap<>(familyMap);
            this.serverFamilyMap = new HashMap<>(serverFamilyMap);
            for (Map.Entry<Integer, List<Long>> entry : serverFamilyMap.entrySet()) {
                FamilyWarRpcHelper.familyWarService().getOnRankFamily(entry.getKey(), FamilyWarConst.W_TYPE_REMOTE, entry.getValue());
                readyServerMap.put(entry.getKey(), false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拉起功能
     *
     * @param serverId
     */
    @Override
    public void startByDisaster(int serverId) {
        String sql = "select * from familywarremotefamily";
        try {
            remote = new FamilyWarRemote();
            Map<Integer, List<Long>> serverFamilyMap = new HashMap<>();
            Map<Long, FamilyWarRemoteFamily> familyMap = new HashMap<>();
            List<FamilyWarRemoteFamily> remoteFamilies = DBUtil.queryList(DBUtil.DB_COMMON, FamilyWarRemoteFamily.class, sql);
            List<FamilyWarRemoteFamily> remoteFamilyList = new ArrayList<>();
            int initBattleType = R_BATTLE_TYPE_32TO16;
            for (FamilyWarRemoteFamily remoteFamily : remoteFamilies) {
                initBattleType = initBattleType > remoteFamily.getBattleType() ? remoteFamily.getBattleType() : initBattleType;
            }
            if (initBattleType == R_BATTLE_TYPE_FINAL) {
                for (FamilyWarRemoteFamily remoteFamily : remoteFamilies) {
                    if (remoteFamily.getBattleType() == initBattleType || remoteFamily.getBattleType() == R_BATTLE_TYPE_3RD4TH) {
                        remoteFamilyList.add(remoteFamily);
                    }
                }
            } else {
                for (FamilyWarRemoteFamily remoteFamily : remoteFamilies) {
                    if (remoteFamily.getBattleType() == initBattleType) {
                        remoteFamilyList.add(remoteFamily);
                    }
                }
            }
            for (FamilyWarRemoteFamily remoteFamily : remoteFamilyList) {
                List<Long> familyIds = serverFamilyMap.get(remoteFamily.getServerId());
                if (familyIds == null) {
                    familyIds = new ArrayList<>();
                    serverFamilyMap.put(remoteFamily.getServerId(), familyIds);
                }
                familyIds.add(remoteFamily.getFamilyId());
                familyMap.put(remoteFamily.getFamilyId(), remoteFamily);
            }
            remote.setIsDisaster(initBattleType);
            if (initBattleType <= R_BATTLE_TYPE_8TO4) {
                long[] tmpLong = new long[16];
                for (FamilyWarRemoteFamily remoteFamily : remoteFamilyList) {
                    for (int index : remoteFamily.getIndexSet()) {
                        tmpLong[index] = remoteFamily.getFamilyId();
                    }
                }
                remote.setDisasterFixture(tmpLong);
            }
            remoteFamilyMap = new HashMap<>(familyMap);
            this.serverFamilyMap = new HashMap<>(serverFamilyMap);
            for (Map.Entry<Integer, List<Long>> entry : serverFamilyMap.entrySet()) {
                FamilyWarRpcHelper.familyWarService().getOnRankFamily(entry.getKey(), FamilyWarConst.W_TYPE_REMOTE, entry.getValue());
                readyServerMap.put(entry.getKey(), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
