package com.stars.modules.familyactivities.war.gm;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.module.Module;
import com.stars.modules.data.DataManager;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFamilyWarEliteFight;
import com.stars.modules.scene.prodata.DynamicBlock;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.prodata.SkillVo;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.FamilyWarUtil;
import com.stars.multiserver.familywar.flow.FamilyWarFlow;
import com.stars.multiserver.familywar.flow.FamilyWarKnockoutFlow;
import com.stars.multiserver.familywar.knockout.fight.elite.FamilyWarEliteFightArgs;
import com.stars.multiserver.fight.FightIdCreator;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.network.PacketUtil;
import com.stars.services.ServiceHelper;
import com.stars.services.family.FamilyAuth;
import com.stars.services.fightServerManager.FSManagerServiceActor;
import com.stars.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stars.modules.familyactivities.war.FamilyActWarManager.stageIdOfEliteFight;
import static java.lang.Long.parseLong;

/**
 * Created by zhaowenshuo on 2016/12/5.
 */
public class FamilyWarGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        FamilyModule familyModule = (FamilyModule) moduleMap.get("family");
        FamilyAuth auth = familyModule.getAuth();
        String cmd = args[0];
        FamilyWarConst.STEP_OF_GENERAL_FLOW = FamilyWarFlow.STEP_LOCAL_KNOCKOUT_START;
        FamilyWarConst.battleType = FamilyWarConst.W_TYPE_LOCAL;
        LogUtil.info("Gm开启家族战相关操作");
        switch (cmd) {
            case "start": //淘汰赛开始
                ServiceHelper.familyWarService().startQualify(MultiServerHelper.getServerId(), FamilyWarConst.W_TYPE_LOCAL);
                ServiceHelper.familyWarLocalService().start(MultiServerHelper.getServerId());
                FamilyWarConst.STEP_OF_SUB_FLOW = FamilyWarKnockoutFlow.STEP_START_KNOCKOUT;
                break;
            case "s8"://四分之一决赛开始
                ServiceHelper.familyWarLocalService().startQuarterFinals();
                FamilyWarConst.STEP_OF_SUB_FLOW = FamilyWarKnockoutFlow.STEP_START_QUARTER_FINALS;
                break;
            case "e8"://四分之一决赛结束
                ServiceHelper.familyWarLocalService().endQuarterFinals();
                FamilyWarConst.STEP_OF_SUB_FLOW = FamilyWarKnockoutFlow.STEP_END_QUARTER_FINALS;
                break;
            case "s4"://二分之一决赛开始
                ServiceHelper.familyWarLocalService().startSemiFinals();
                FamilyWarConst.STEP_OF_SUB_FLOW = FamilyWarKnockoutFlow.STEP_START_SEMI_FINALS;
                break;
            case "e4"://二分之一决赛开始
                ServiceHelper.familyWarLocalService().endSemiFinals();
                FamilyWarConst.STEP_OF_SUB_FLOW = FamilyWarKnockoutFlow.STEP_END_SEMI_FINALS;
                break;
            case "s2"://决赛开始
                ServiceHelper.familyWarLocalService().startFinal();
                FamilyWarConst.STEP_OF_SUB_FLOW = FamilyWarKnockoutFlow.STEP_START_FINALS;
                break;
            case "e2"://决赛开始
                ServiceHelper.familyWarLocalService().endFinal();
                FamilyWarConst.STEP_OF_SUB_FLOW = FamilyWarKnockoutFlow.STEP_END_FINALS;
                break;
            case "match":
                ServiceHelper.familyWarLocalService().match();
                break;
            case "odd":
                FamilyActWarManager.familywar_pairstageodd = Integer.parseInt(args[1]);
                break;
            case "schedule":
                try {
                    FamilyWarKnockoutFlow flow = new FamilyWarKnockoutFlow();
                    flow.setLocalService(ServiceHelper.familyWarLocalService());
                    flow.init(SchedulerHelper.getScheduler(), DataManager.getActivityFlowConfig(3000));
                } catch (Exception e) {
                    LogUtil.error("", e);
                }
                break;
            case "trigger":
                FamilyWarFlow.isLocalRunning = true;
                ServiceHelper.familyWarService().createFamilyInfo();
                ServiceHelper.familyWarLocalService().startByDisaster(MultiServerHelper.getServerId());
                break;
            case "closeai":
                FamilyWarConst.openAI = false;
                break;
            case "openai":
                FamilyWarConst.openAI = true;
                break;
            case "fightinit":
                int outTime = Integer.parseInt(args[1]);
                FamilyWarTestManager ftm = new FamilyWarTestManager();
                ftm.init(outTime);
                break;
            case "fight":
                int count = Integer.parseInt(args[1]);
                fightDemo(count, moduleMap);
                break;
        }
    }

    private void fightDemo(int count, Map<String, Module> moduleMap) {
        for (int i = 0; i < count; i++) {
            int serverId = ServiceHelper.fsManagerService().getFightServer(FSManagerServiceActor.FIGHT_SERVER_LEVEL_COMM);
            String fightId = FightIdCreator.creatUUId();
            Map<String, FighterEntity> fighterMap = getFighterEntity(i, moduleMap);
            MainRpcHelper.fightBaseService().createFight(serverId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                    MultiServerHelper.getServerId(), fightId, createEnterEliteFightPacket(), getDemoArgs(fightId, fighterMap));
            Map<String, FighterEntity> nonPlayerEntity = FamilyWarUtil.getMonsterFighterEntity(stageIdOfEliteFight);
            MainRpcHelper.fightBaseService().addMonster(serverId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                    MultiServerHelper.getServerId(), fightId, new ArrayList<>(nonPlayerEntity.values()));
            List<FighterEntity> entityList = new ArrayList<>();
            entityList.addAll(fighterMap.values());
            MainRpcHelper.fightBaseService().addFighterNotSend(serverId, FightConst.T_FAMILY_WAR_ELITE_FIGHT,
                    MultiServerHelper.getServerId(), fightId, new ArrayList<>(entityList));
            FamilyWarTestManager.addFightId(fightId, serverId);
        }
    }

    private FamilyWarEliteFightArgs getDemoArgs(String fightId, Map<String, FighterEntity> fighterMap) {
        FamilyWarEliteFightArgs args = new FamilyWarEliteFightArgs();
        args.setBattleId(fightId);
        args.setCamp1MainServerId(MultiServerHelper.getServerId());
        args.setCamp2MainServerId(MultiServerHelper.getServerId());
        args.setCreateTimestamp(System.currentTimeMillis());
        Map<Long, Byte> campMap = new HashMap<>();
        Map<Long, Integer> roleWarType = new HashMap<>();
        for (FighterEntity entity : fighterMap.values()) {
            campMap.put(parseLong(entity.getUniqueId()), entity.getCamp());
            roleWarType.put(parseLong(entity.getUniqueId()), 1);
        }
        args.setCampMap(campMap);
        args.setRoleWarType(roleWarType);
        return args;
    }

    private Map<String, FighterEntity> getFighterEntity(int count, Map<String, Module> moduleMap) {
        Map<String, FighterEntity> tmp = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            FighterEntity entity = FighterCreator.createSelf(moduleMap, (byte) 1);
            tmp.put("" + count + "-" + i + "-" + entity.getUniqueId(), entity);
        }
        for (int i = 0; i < 5; i++) {
            FighterEntity entity = FighterCreator.createSelf(moduleMap, (byte) 2);
            tmp.put("" + count + "-" + i + "-" + entity.getUniqueId(), entity);
        }
        return tmp;
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

}
