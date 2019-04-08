package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
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
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;

import java.util.*;

/**
 * Created by daiyaorong on 2016/11/15.
 */
public class NewGuideScene extends FightScene {

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object obj) {
        return true;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object obj) {
        Map<String, String> defineMap = SceneManager.getFcdMap();
        this.stageId = Integer.parseInt(defineMap.get("newguidedungeon"));//进入此处前已进行过有效性判断
        this.startTimestamp = System.currentTimeMillis();
        this.stageStatus = SceneManager.STAGE_PROCEEDING;
        this.isAgain = (byte) 0;
        StageinfoVo stageVo = SceneManager.getStageVo(this.stageId);
        ClientEnterDungeon enterFight = new ClientEnterDungeon();
        requestSendClientEnterFight(moduleMap, enterFight, stageVo);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        sceneModule.send(enterFight);
    }

    private void requestSendClientEnterFight(Map<String, Module> moduleMap, ClientEnterDungeon enterFight,
                                             StageinfoVo stageVo) {
        enterFight.setIsAgain(this.isAgain);
        enterFight.setStageId(stageVo.getStageId());
        enterFight.setFightType(stageVo.getStageType());
        List<FighterEntity> fighterList = new LinkedList<>();
        /* 出战角色 */
        FighterEntity roleEntity = FighterCreator.createSelf(moduleMap, stageVo.getPosition(), stageVo.getRotation());
        entityMap.put(roleEntity.getUniqueId(), roleEntity);
        /* 预加载怪物 */
        initMonsterData(moduleMap, enterFight, stageVo);
        fighterList.addAll(entityMap.values());
        enterFight.setFighterEntityList(fighterList);
        /* 动态阻挡数据 */
        initDynamicBlockData(enterFight, stageVo);
    }

    @Override
    public void exit(Map<String, Module> moduleMap) {

    }

    @Override
    public boolean isEnd() {
        return false;
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

    @Override
    public void updateTimeExecute(Map<String, Module> moduleMap) {

    }

    @Override
    public void finishDeal(Map<String, Module> moduleMap, byte finish) {
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        if (finish == SceneManager.STAGE_VICTORY) {
            try {
                // 入库
                RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
                roleModule.context().recordMap().setString("isPassNewGuideDungeon", "1");
            } catch (Throwable t) {
                com.stars.util.LogUtil.error("", t);
            }
            this.endTimestamp = System.currentTimeMillis();
            ClientStageFinish clientStageFinish = new ClientStageFinish(SceneManager.SCENETYPE_NEWGUIDE, finish);
            clientStageFinish.setStar((byte) 0);
            clientStageFinish.setUseTime((int) Math.floor((endTimestamp - startTimestamp) / 1000.0));
            sceneModule.send(clientStageFinish);
        }
//        else{
//            sceneModule.enterScene(SceneManager.SCENETYPE_NEWGUIDE, 0, "");
//        }
    }

    @Override
    public void enemyDead(Map<String, Module> moduleMap, List<String> uIdList) {
        LogUtil.info("怪物死亡前检测状态0:{}", stageStatus);
        // 刷怪数据
        Map<String, FighterEntity> sendMonsterMap = new HashMap<>();
        // 销毁陷阱怪数据
        List<String> destroyTrapMonsterList = new ArrayList<String>();
        // 动态阻挡状态改变
        Map<String, Byte> blockStatusMap = new HashMap<>();
        for (String monsterUId : uIdList) {
            FighterEntity monsterEntity = entityMap.get(monsterUId);
            if (monsterEntity == null) {
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
        // 胜利失败检测
        checkFinish(uIdList);
        if (stageStatus != SceneManager.STAGE_PROCEEDING) {
            finishDeal(moduleMap, stageStatus);
        }
        // 如果还有下波怪,继续刷
        if (blockStatusMap.size() == 0 && sendMonsterMap.size() == 0) {
            return;
        }
        ClientSpawnMonster clientSpawnMonster = new ClientSpawnMonster();
        clientSpawnMonster.setBlockStatusMap(blockStatusMap);
        clientSpawnMonster.setSpawnMonsterMap(sendMonsterMap);
        clientSpawnMonster.setDestroyTrapMonsterList(destroyTrapMonsterList);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        clientSpawnMonster.setSpawinId(spawnSeq);
        sceneModule.send(clientSpawnMonster);
        addResendPacket(spawnSeq, clientSpawnMonster);
    }

    @Override
    public void defeatCheckTime(int conditonParam) {

    }

    @Override
    public void selfDead(Map<String, Module> moduleMap) {
        super.selfDead(moduleMap);
    }
}
