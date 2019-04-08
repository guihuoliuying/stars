package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropModule;
import com.stars.modules.dungeon.DungeonManager;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.prodata.ProduceDungeonVo;
import com.stars.modules.guardofficial.GuardOfficialModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientSpawnMonster;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterDungeon;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.ServerLogConst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-07-12.
 */
public class GuardOfficialScene extends FightScene {
    private byte produceDungeonType;// 产出副本类型;坐骑or强化石副本
    private int produceDungeonId;

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object obj) {
        GuardOfficialModule module = (GuardOfficialModule) moduleMap.get(MConst.GuardOfficial);
        return module.canEnterScene();
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object obj) {
        String tmpStr = (String) obj;
        byte type = Byte.parseByte(tmpStr.split("-")[0]);
        int stageId = Integer.parseInt(tmpStr.split("-")[1]);
        DungeonModule dungeonModule = (DungeonModule) moduleMap.get(MConst.Dungeon);
        ProduceDungeonVo produceDungeonVo = dungeonModule.getEnterProduceDungeonVo(type);
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        if (stageVo == null) {
            return;
        }
        this.stageId = stageId;
        this.startTimestamp = System.currentTimeMillis();
        this.stageStatus = SceneManager.STAGE_PROCEEDING;
        ClientEnterDungeon enterFight = new ClientEnterDungeon();
        requestSendClientEnterFight(moduleMap, enterFight, stageVo);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        // 先发剧情播放记录
        sceneModule.sendPlayedDrama(this, this.stageId);
        sceneModule.send(enterFight);
        this.produceDungeonType = type;
        this.produceDungeonId = produceDungeonVo.getProduceDungeonId();
        ServerLogModule logModule = (ServerLogModule) moduleMap.get(MConst.ServerLog);
        logModule.Log_core_activity(ServerLogConst.ACTIVITY_START, ThemeType.ACTIVITY_43.getThemeId(), logModule.makeJuci(),
                ThemeType.ACTIVITY_39.getThemeId(), stageId, 0);
    }

    protected void requestSendClientEnterFight(Map<String, Module> moduleMap, ClientEnterDungeon enterFight,
                                               StageinfoVo stageVo) {
        enterFight.setIsAgain(isAgain);
        enterFight.setStageId(stageId);
        enterFight.setFightType(stageVo.getStageType());
        /* 出战角色 */
        FighterEntity roleEntity = FighterCreator.createSelf(moduleMap, stageVo.getPosition(), stageVo.getRotation());
        entityMap.put(roleEntity.getUniqueId(), roleEntity);
        /* 出战伙伴 */
        BuddyModule buddyModule = (BuddyModule) moduleMap.get(MConst.Buddy);
        // 有出战伙伴
        if (buddyModule.getFightBuddyId() != 0 && Integer.parseInt(DataManager.getCommConfig("dragonboat_dungeon_buddy")) == 1) {
            FighterEntity buddyEntity = FighterCreator.create(FighterEntity.TYPE_BUDDY, FighterEntity.CAMP_SELF,
                    buddyModule.getRoleBuddy(buddyModule.getFightBuddyId()));
            entityMap.put(buddyEntity.getUniqueId(), buddyEntity);
        }
        initMonsterData(moduleMap, enterFight, stageVo);
        enterFight.setFighterEntityList(entityMap.values());
        /* 动态阻挡数据 */
        initDynamicBlockData(enterFight, stageVo);
        /* 是否自动战斗 */
        RoleModule rm = (RoleModule) moduleMap.get(MConst.Role);
        enterFight.setAutoFlag(rm.getAutoFightFlag(enterFight.getFightType()));
        /* 副本失败时间 */
        if (stageVo.getFailConMap().containsKey(SceneManager.FAIL_CONDITION_TIME)) {
            enterFight.setFailTime(stageVo.getFailConMap().get(SceneManager.FAIL_CONDITION_TIME));
        } else if (stageVo.getVictoryConMap().containsKey(SceneManager.VICTORY_CONDITION_TIME)) {
            enterFight.setFailTime(stageVo.getVictoryConMap().get(SceneManager.VICTORY_CONDITION_TIME));
        }
    }

    @Override
    public void exit(Map<String, Module> moduleMap) {

    }

    @Override
    public boolean isEnd() {
        return false;
    }

    @Override
    public void finishDeal(Map<String, Module> moduleMap, byte finish) {
        this.endTimestamp = System.currentTimeMillis();
        dropItemSwitch((RoleModule) moduleMap.get(MConst.Role));
        ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
        toolModule.addAndSend(totalDropMap, EventType.DUNGEONSCENE.getCode());
        //增加通关奖励
        ProduceDungeonVo produceDungeonVo = DungeonManager.getProduceDungeonVo(produceDungeonType, produceDungeonId);
        DropModule dropModule = (DropModule) moduleMap.get(MConst.Drop);
        Map<Integer, Integer> reward = dropModule.executeDrop(produceDungeonVo.getDropId(), 1, true);
        Map<Integer, Integer> map = toolModule.addAndSend(reward, EventType.DUNGEONSCENE.getCode());
        com.stars.util.MapUtil.add(totalDropMap, map);
//        totalDropMap.putAll(map);
        ClientStageFinish clientStageFinish = new ClientStageFinish(SceneManager.SCENETYPE_BUDDY_DUNGEON, finish);
        clientStageFinish.setItemMap(totalDropMap);
        clientStageFinish.setUseTime((int) Math.floor((endTimestamp - startTimestamp) / 1000.0));
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        sceneModule.send(clientStageFinish);
        if (finish == SceneManager.STAGE_VICTORY) {
            GuardOfficialModule officialModule = (GuardOfficialModule) moduleMap.get(MConst.GuardOfficial);
            officialModule.finishEvent();
        }
        byte logType = finish == SceneManager.STAGE_VICTORY ? ServerLogConst.ACTIVITY_WIN : ServerLogConst.ACTIVITY_FAIL;
        ServerLogModule logModule = (ServerLogModule) moduleMap.get(MConst.ServerLog);
        logModule.Log_core_activity(logType, ThemeType.ACTIVITY_43.getThemeId(), logModule.makeJuci(),
                ThemeType.ACTIVITY_43.getThemeId(), stageId, (endTimestamp - startTimestamp) / 1000);
    }

    @Override
    public void enemyDead(Map<String, Module> moduleMap, List<String> uIdList) {
        Map<String, FighterEntity> sendMonsterMap = new HashMap<>();
        // 销毁陷阱怪数据
        List<String> destroyTrapMonsterList = new ArrayList<String>();
        // 动态阻挡状态改变
        Map<String, Byte> blockStatusMap = new HashMap<>();
        for (String monsterUId : uIdList) {
            FighterEntity monsterEntity = entityMap.get(monsterUId);
            if (monsterEntity == null) {
                LogUtil.info("monster dead but not get entity monsteruid=" + monsterUId);
                continue;
            }
            spawnMapping.get(monsterEntity.getSpawnUId()).remove(monsterUId);

            MonsterSpawnVo spawnVo = SceneManager.getMonsterSpawnVo(monsterEntity.getSpawnConfigId());
            // 刷怪组全部死亡,关闭动态阻挡
            if (spawnMapping.get(monsterEntity.getSpawnUId()).isEmpty()) {
                blockStatusMap.putAll(closeBlock(monsterEntity.getSpawnConfigId()));
            }
            // 判断下一波刷怪条件
            if (spawnMapping.get(monsterEntity.getSpawnUId()).size() == Integer.parseInt(spawnVo.getNextConParam())) {
                if (spawnVo.getNextSpawnId() != 0) {
                    sendMonsterMap.putAll(spawnMonster(moduleMap, spawnVo.getNextSpawnId()));
                    destroyTrapMonsterList = destroyTrapMonster(spawnVo.getNextSpawnId());
                    blockStatusMap.putAll(openBlock(spawnVo.getNextSpawnId()));
                }
            }
            // 怪物死亡增加掉落
            MapUtil.add(totalDropMap, monsterEntity.getDropMap());
        }
        StageinfoVo stageVo = SceneManager.getStageVo(this.stageId);
        // 胜利失败检测
        if (stageStatus == SceneManager.STAGE_PROCEEDING || stageStatus == SceneManager.STAGE_PAUSE) {
            checkFinish(uIdList);
            if (stageStatus != SceneManager.STAGE_PROCEEDING && stageStatus != SceneManager.STAGE_PAUSE) {
                finishDeal(moduleMap, stageStatus);
            }
        }
        // 如果还有下波怪,继续刷
        if (blockStatusMap.size() == 0 && sendMonsterMap.size() == 0) {
            return;
        }
        ClientSpawnMonster clientSpawnMonster = new ClientSpawnMonster();
        clientSpawnMonster.setSpawinId(spawnSeq);
        clientSpawnMonster.setBlockStatusMap(blockStatusMap);
        clientSpawnMonster.setSpawnMonsterMap(sendMonsterMap);
        clientSpawnMonster.setDestroyTrapMonsterList(destroyTrapMonsterList);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        clientSpawnMonster.setSpawinId(spawnSeq);
        sceneModule.send(clientSpawnMonster);
        addResendPacket(spawnSeq, clientSpawnMonster);
    }

    @Override
    public void areaSpawnMonster(Map<String, Module> moduleMap, int spawnId) {
        if (areaSpawnStateMap.containsKey(spawnId) && !areaSpawnStateMap.get(spawnId)) {
            ClientSpawnMonster clientSpawnMonster = new ClientSpawnMonster();
            clientSpawnMonster.setBlockStatusMap(openBlock(spawnId));
            clientSpawnMonster.setSpawnMonsterMap(spawnMonster(moduleMap, spawnId));
            clientSpawnMonster.setDestroyTrapMonsterList(destroyTrapMonster(spawnId));
            areaSpawnStateMap.put(spawnId, true);
            SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
            clientSpawnMonster.setSpawinId(spawnSeq);
            sceneModule.send(clientSpawnMonster);
            addResendPacket(spawnSeq, clientSpawnMonster);
        }
    }

    private void dropItemSwitch(RoleModule roleModule) {
        ProduceDungeonVo produceDungeonVo = DungeonManager.getProduceDungeonVo(produceDungeonType, produceDungeonId);
        Map<Integer, Integer> map = new HashMap<>();
        map.putAll(totalDropMap);
        // 遍历怪物掉落
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getKey() == ToolManager.EXP && roleModule.isRoleLevelMax()) {
                for (Map.Entry<Integer, Double> switchEntry : produceDungeonVo.getExpSwitchMap().entrySet()) {
                    totalDropMap.put(switchEntry.getKey(), (int) Math.ceil(entry.getValue() * switchEntry.getValue()));
                }
                totalDropMap.remove(ToolManager.EXP);
            }
        }
    }
}
