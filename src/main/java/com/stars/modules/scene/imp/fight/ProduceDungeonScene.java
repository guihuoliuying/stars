package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.drop.DropModule;
import com.stars.modules.dungeon.DungeonManager;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.prodata.ProduceDungeonVo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientSpawnMonster;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.packet.ServerProduceDungeonReward;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterDungeon;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterProduceDungeon;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/9/1.
 */
public class ProduceDungeonScene extends DungeonScene {
    private byte produceDungeonType;// 产出副本类型;坐骑or强化石副本
    private int produceDungeonId;

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object pdType) {
        byte type = Byte.parseByte(pdType.toString());
        DungeonModule dungeonModule = (DungeonModule) moduleMap.get(MConst.Dungeon);
        ProduceDungeonVo produceDungeonVo = dungeonModule.getEnterProduceDungeonVo(type);
        if (produceDungeonVo == null) {
            dungeonModule.warn("找不到等级对应副本");
            return false;
        }
        StageinfoVo stageinfoVo = SceneManager.getStageVo(produceDungeonVo.getStageId());
        if (stageinfoVo == null || !stageinfoVo.getVictoryConMap().containsKey(SceneManager.VICTORY_CONDITION_TIME)) {
            dungeonModule.warn("战斗场景配置错误stageId=" + produceDungeonVo.getStageId());
            return false;
        }
        this.produceDungeonType = type;
        this.produceDungeonId = produceDungeonVo.getProduceDungeonId();
        this.stageId = stageinfoVo.getStageId();
        return true;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object pdType) {
        this.startTimestamp = System.currentTimeMillis();
        this.stageStatus = SceneManager.STAGE_PROCEEDING;
        ClientEnterProduceDungeon enterProduceDungeon = new ClientEnterProduceDungeon();
        requestSendClientEnterFight(moduleMap, enterProduceDungeon, SceneManager.getStageVo(stageId));
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        enterProduceDungeon.setLimitTime(stageVo.getVictoryConMap().get(SceneManager.VICTORY_CONDITION_TIME));
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        // 先发剧情播放记录
        sceneModule.sendPlayedDrama(this, stageId);
        sceneModule.send(enterProduceDungeon);
        ProduceDungeonVo produceDungeonVo = DungeonManager.getProduceDungeonVo(produceDungeonType, produceDungeonId);
        // 抛出每日活动事件
        sceneModule.dispatchEvent(new DailyFuntionEvent((short) produceDungeonVo.getDailyId(produceDungeonType), 1));
        // 开始日志

    }

    @Override
    public void exit(Map<String, Module> moduleMap) {
        if (stageStatus == SceneManager.STAGE_PROCEEDING || stageStatus == SceneManager.STAGE_PAUSE) {
            dropItemSwitch((RoleModule) moduleMap.get(MConst.Role));
            ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
            toolModule.addAndSend(totalDropMap, EventType.DUNGEONSCENE.getCode());
            // 退出日志

        }
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
        ClientStageFinish clientStageFinish = new ClientStageFinish(SceneManager.SCENETYPE_PRODUCEDUNGEON, finish);
        clientStageFinish.setItemMap(totalDropMap);
        clientStageFinish.setUseTime((int) Math.floor((endTimestamp - startTimestamp) / 1000.0));
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        sceneModule.send(clientStageFinish);
        // 结束日志

    }

    @Override
    public void receivePacket(Map<String, Module> moduleMap, PlayerPacket packet) {
        super.receivePacket(moduleMap, packet);
        if (packet instanceof ServerProduceDungeonReward) {
            ServerProduceDungeonReward rewardPacket = (ServerProduceDungeonReward) packet;
            ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
            RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
            // 是否翻倍领取
            boolean isDouble = rewardPacket.getIsDouble() == 1;
            if (isDouble) {
                ProduceDungeonVo produceDungeonVo = DungeonManager.getProduceDungeonVo(produceDungeonType, produceDungeonId);
                if (toolModule.deleteAndSend(produceDungeonVo.getDoubleCostMap(), EventType.DUNGEONSCENE.getCode())) {
                    Map<Integer, Double> rateMap = produceDungeonVo.getDoubleItemRate();
                    for (Map.Entry<Integer, Integer> entry : totalDropMap.entrySet()) {
                        // 配置物品翻倍
                        if (rateMap.containsKey(entry.getKey()))
                            totalDropMap.put(entry.getKey(), (int) Math.ceil(entry.getValue() * rateMap.get(entry.getKey())));
                        // 经验转换物品翻倍
                        if (roleModule.isRoleLevelMax() && produceDungeonVo.getExpSwitchMap().containsKey(entry.getKey()))
                            totalDropMap.put(entry.getKey(), (int) Math.ceil(entry.getValue() * rateMap.get(ToolManager.EXP)));
                    }
                }
            }
            toolModule.addAndSend(totalDropMap, EventType.DUNGEONSCENE.getCode());
        }
    }

    @Override
    protected void initMonsterData(Map<String, Module> moduleMap, ClientEnterDungeon enterFight, Object obj) {
        ClientEnterProduceDungeon enterFightPacket = (ClientEnterProduceDungeon) enterFight;
        super.initMonsterData(moduleMap, enterFightPacket, obj);
        StageinfoVo stageVo = (StageinfoVo) obj;
        List<Integer> checkLoop = new ArrayList<>();
        for (int monsterSpawnId : stageVo.getMonsterSpawnIdList()) {
            MonsterSpawnVo monsterSpawnVo = SceneManager.getMonsterSpawnVo(monsterSpawnId);
            if (monsterSpawnVo.getSpawnType() == 1) {
                checkLoop.add(monsterSpawnVo.getMonsterSpawnId());
                getNextMonsterSpawn(monsterSpawnVo, checkLoop);
            }
        }
        List<String> tempStr = new ArrayList<>();
        for (Integer monsterSpawnId : checkLoop) {
            MonsterSpawnVo monsterSpawnVo = SceneManager.getMonsterSpawnVo(monsterSpawnId);
            tempStr.add(monsterSpawnVo.getMonsterTypeCountStr());
        }
        enterFightPacket.setWaveMonsterTypeList(tempStr);
    }

    private List<Integer> getNextMonsterSpawn(MonsterSpawnVo monsterSpawnVo, List<Integer> monsterSpawnIdList) {
        MonsterSpawnVo nextVo = SceneManager.getMonsterSpawnVo(monsterSpawnVo.getNextSpawnId());
        if (nextVo == null) {
            return monsterSpawnIdList;
        }
        if (nextVo.getMonsterSpawnId() == 0) {
            return monsterSpawnIdList;
        } else {
            if (monsterSpawnIdList.contains(nextVo.getMonsterSpawnId())) {
                LogUtil.error("资源副本的刷怪数据配置有问题,Spawn nextSpawnId不能相互指向, 会造成死循环问题！:{}", nextVo.getMonsterSpawnId());
                return monsterSpawnIdList;
            } else {
                monsterSpawnIdList.add(nextVo.getMonsterSpawnId());
                return getNextMonsterSpawn(nextVo, monsterSpawnIdList);
            }
        }
    }

    @Override
    public void enemyDead(Map<String, Module> moduleMap, List<String> uIdList) {
        if (stageStatus == SceneManager.STAGE_VICTORY || stageStatus == SceneManager.STAGE_FAIL) {
            return;
        }

        // 刷怪数据
        Map<String, FighterEntity> sendMonsterMap = new HashMap<>();
        // 销毁陷阱怪数据
        List<String> destroyTrapMonsterList = new ArrayList<>();
        // 动态阻挡状态改变
        Map<String, Byte> blockStatusMap = new HashMap<>();
        // 死亡怪物的刷怪组,刷怪组唯一Id-刷怪组配置Id
        Map<String, Integer> spawnUIdMap = new HashMap<>();
        for (String monsterUId : uIdList) {
            FighterEntity monsterEntity = entityMap.get(monsterUId);
            if (monsterEntity == null) {
                continue;
            }
            spawnUIdMap.put(monsterEntity.getSpawnUId(), monsterEntity.getSpawnConfigId());
            spawnMapping.get(monsterEntity.getSpawnUId()).remove(monsterUId);
            MapUtil.add(totalDropMap, monsterEntity.getDropMap());
        }
        for (Map.Entry<String, Integer> entry : spawnUIdMap.entrySet()) {
            // 刷怪组全部死亡处理
            if (spawnMapping.get(entry.getKey()).isEmpty()) {
                blockStatusMap.putAll(closeBlock(entry.getValue()));
                MonsterSpawnVo spawnVo = SceneManager.getMonsterSpawnVo(entry.getValue());
                // todo:有下一波刷怪条件的时候要判断
                if (spawnVo.getNextSpawnId() != 0) {
                    sendMonsterMap.putAll(spawnMonster(moduleMap, spawnVo.getNextSpawnId()));
                    destroyTrapMonsterList = destroyTrapMonster(spawnVo.getNextSpawnId());
                    blockStatusMap.putAll(openBlock(spawnVo.getNextSpawnId()));
                }
            }
        }
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
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
        clientSpawnMonster.setSpawinId(spawnSeq);
        sceneModule.send(clientSpawnMonster);
        addResendPacket(spawnSeq, clientSpawnMonster);
    }

    private short getAchievementId() {
        short achievementId = 0;
        return achievementId;
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
