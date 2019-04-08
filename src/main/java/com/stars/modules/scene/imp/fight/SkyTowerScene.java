package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.achievement.event.JoinActivityEvent;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientSpawnMonster;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterDungeon;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterSkyTower;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.skytower.SkyTowerManager;
import com.stars.modules.skytower.SkyTowerModule;
import com.stars.modules.skytower.prodata.SkyTowerVo;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.ServerLogConst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 镇妖塔战斗场景;
 * Created by panzhenfeng on 2016/8/10.
 */
public class SkyTowerScene extends DungeonScene {
    private SkyTowerVo curSkyTowerVo = null;
    private int timeLimitMillSeconds = 0;

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object layerId) {
        SkyTowerModule skyTowerModule = (SkyTowerModule) moduleMap.get(MConst.SkyTower);
        int tmpCurLayerId = skyTowerModule.getRecordMapSkyTower().getCurLayerId();
        boolean isCanDo = false;
        do {
            //只能进入当前所在层次;
            if (tmpCurLayerId != (int) layerId) {
                skyTowerModule.warn("不能进入非当前层的镇妖塔场景");
                break;
            }
            SkyTowerVo skyTowerVo = SkyTowerManager.getSkyTowerById((int) layerId);
            //判断是否等级足够;
            RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
            if (skyTowerVo.getLevellimit() > roleModule.getLevel()) {
                skyTowerModule.warn("skytower_enterlevellimit", Integer.toString(skyTowerVo.getLevellimit()));
                break;
            }
            isCanDo = true;
        } while (false);
        return isCanDo;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object layerId) {
        SkyTowerModule skyTowerModule = (SkyTowerModule) moduleMap.get(MConst.SkyTower);
        skyTowerModule.checkDailyEvent();
        curSkyTowerVo = SkyTowerManager.getSkyTowerById((int) layerId);
        timeLimitMillSeconds = curSkyTowerVo.getTimelimit() * 1000;
        StageinfoVo stageVo = SceneManager.getStageVo(curSkyTowerVo.getStageid());
        this.stageId = stageVo.getStageId();
        this.startTimestamp = System.currentTimeMillis();
        this.stageStatus = SceneManager.STAGE_PROCEEDING;
        //进来时即暂停计时，由客户端通知进行调用开始计时;
        this.pauseFightTime();
        ClientEnterSkyTower enterSkyTower = new ClientEnterSkyTower();
        requestSendClientEnterFight(moduleMap, enterSkyTower, stageVo);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        // 先发剧情播放记录
        sceneModule.sendPlayedDrama(this, stageId);
        sceneModule.send(enterSkyTower);
        //打印日志;
        ServerLogModule serverLogModule = (ServerLogModule) moduleMap.get(MConst.ServerLog);
        serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_START, ThemeType.ACTIVITY_11.getThemeId(), curSkyTowerVo.getStageid());
    }

    @Override
    public void enemyDead(Map<String, Module> moduleMap, List<String> uIdList) {
        // 刷怪数据
        Map<String, FighterEntity> sendMonsterMap = new HashMap<>();
        // 销毁陷阱怪数据
        List<String> destroyTrapMonsterList = new ArrayList<String>();
        // 动态阻挡状态改变
        Map<String, Byte> blockStatusMap = new HashMap<>();
        // 死亡怪物的刷怪组,刷怪组唯一Id-刷怪组配置Id
        Map<String, Integer> spawnUIdMap = new HashMap<>();
        for (String monsterUId : uIdList) {
            FighterEntity monsterEntity = entityMap.get(monsterUId);
//            MonsterAttributeVo monsterAttr = spawnMonsterMap.get(monsterUId);
            if (monsterEntity == null) {
                continue;
            }
            spawnUIdMap.put(monsterEntity.getSpawnUId(), monsterEntity.getSpawnConfigId());
            spawnMapping.get(monsterEntity.getSpawnUId()).remove(monsterUId);

            // 怪物死亡增加掉落
//            StringUtil.combineIntegerMap(totalDropMap, monsterAttr.getDropMap());
            com.stars.util.MapUtil.add(totalDropMap, monsterEntity.getDropMap());
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

    @Override
    public void finishDeal(Map<String, Module> moduleMap, byte finish) {
        SkyTowerModule skyTowerModule = (SkyTowerModule) moduleMap.get(MConst.SkyTower);
        this.endTimestamp = System.currentTimeMillis();
        // 关卡掉落
        Map<Integer, Integer> rewardMap = totalDropMap;
        int passedUseTime = (int) Math.floor((endTimestamp - startTimestamp) / 1000.0);
        // 胜利通关
        if (finish == SceneManager.STAGE_VICTORY) {
            SkyTowerVo tmpSkyTowerVo = curSkyTowerVo;
            //镇妖塔有跳层的逻辑，这里需要判断下是否有跳层数据;
            int addedJumpLayerCount = tmpSkyTowerVo.getCanJumpAddedLayer(passedUseTime);
            //胜利了,将今天的奖励吃
            do {
                //判断是不是首次, 首次通关奖励直接发往背包;
                if (skyTowerModule.isFirstPass(tmpSkyTowerVo.getLayerId())) {
                    com.stars.util.MapUtil.add(rewardMap, tmpSkyTowerVo.getFirstPassRewardsMap());
                }
                //挑战奖励,直接发送到背包中;
                if (skyTowerModule.isOpenedChallengeRewardLimit()) {
                    Map<Integer, Integer> challengeSucRewardsMap = tmpSkyTowerVo.getChallengeSucRewardsMap();
                    //判断该层是否有挑战成功奖励,有的话才发送累计的失败奖励池,判断是否有累计的失败奖励，也要一同下发;
                    if (challengeSucRewardsMap != null && challengeSucRewardsMap.size() > 0 &&
                            skyTowerModule.getRecordMapSkyTower().getCurLayerIsPass() != (byte) 1 &&
                            !skyTowerModule.getRecordMapSkyTower().isHistoryPassed() ) { //以前通过了不再发奖
                        com.stars.util.MapUtil.add(rewardMap, challengeSucRewardsMap);
                        MapUtil.add(rewardMap, skyTowerModule.getRecordMapSkyTower().getPreFailChallengeAwardMap());
                        skyTowerModule.getRecordMapSkyTower().removeAllPreChallengeAwardLayerId();
                    }
                }
                //设置当前层通关了;
                skyTowerModule.getRecordMapSkyTower().setCurLayerIsPass((byte) 1);
                //设置下一层数据;
                skyTowerModule.getRecordMapSkyTower().setNextLayerId();
                skyTowerModule.fireEvent(new JoinActivityEvent(JoinActivityEvent.SKYTOWER));
                if (!skyTowerModule.isCanJumpToLayer(skyTowerModule.getRecordMapSkyTower().getCurLayerId())) {
                    break;
                }
                tmpSkyTowerVo = SkyTowerManager.getSkyTowerById(skyTowerModule.getRecordMapSkyTower().getCurLayerId());
            } while ((addedJumpLayerCount--) > 0);
            //下发获取到的奖励物品;
            ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
            toolModule.addAndSend(rewardMap, EventType.SKYTOWER.getCode());
            //通知服务端镇妖塔数据的变化;
            skyTowerModule.syncToClientSkyTowerInfo();
            //打印日志;
            ServerLogModule serverLogModule = (ServerLogModule) moduleMap.get(MConst.ServerLog);
            serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_WIN, ThemeType.ACTIVITY_11.getThemeId(), tmpSkyTowerVo.getStageid());
        } else if (finish == SceneManager.STAGE_FAIL) {
            //打印日志;
            RoleModule roleM = (RoleModule) moduleMap.get(MConst.Role);
            ServerLogModule serverLogModule = (ServerLogModule) moduleMap.get(MConst.ServerLog);
            serverLogModule.Log_core_activity(ServerLogConst.ACTIVITY_FAIL, ThemeType.ACTIVITY_11.getThemeId(), curSkyTowerVo == null ? roleM.getRoleRow().getSafeStageId() : curSkyTowerVo.getStageid());
        }
        ClientStageFinish clientStageFinish = new ClientStageFinish(SceneManager.SCENETYPE_SKYTOWER, finish);
        //要过滤掉自动使用的数据显示到客户端;
        ItemVo tmpItemVo = null;
        List<Integer> waitToRemoveIdList = new ArrayList<>();
        for (Map.Entry<Integer, Integer> kvp : rewardMap.entrySet()) {
            tmpItemVo = ToolManager.getItemVo(kvp.getKey());
            if (tmpItemVo != null) {
                if (tmpItemVo.isAutoUse()) {
                    waitToRemoveIdList.add(tmpItemVo.getItemId());
                }
            }
        }
        for (int i = 0, len = waitToRemoveIdList.size(); i < len; i++) {
            rewardMap.remove(waitToRemoveIdList.get(i));
        }
        clientStageFinish.setItemMap(rewardMap);
        clientStageFinish.setUseTime(passedUseTime);
        skyTowerModule.send(clientStageFinish);
    }

    @Override
    public void updateTimeExecute(Map<String, Module> moduleMap) {
        if (this.stageStatus == SceneManager.STAGE_PROCEEDING) {
            // 胜利失败检测
            checkFinish(null);
            if (stageStatus != SceneManager.STAGE_PROCEEDING) {
                finishDeal(moduleMap, stageStatus);
            }
        }
        resendPacket(moduleMap);
    }

    @Override
    public void checkFinish(List<String> uIdList) {
        //失败检测;
        if ((System.currentTimeMillis() - startTimestamp >= timeLimitMillSeconds)) {
            this.stageStatus = SceneManager.STAGE_FAIL;
            return;
        }
        //检测胜利;
        boolean allDead = true;
        for (List<String> monsterUIdList : spawnMapping.values()) {
            if (!monsterUIdList.isEmpty()) {// 刷出怪物没死完
                allDead = false;
                break;
            }
        }
        if (allDead) {
            this.stageStatus = SceneManager.STAGE_VICTORY;
            return;
        }
    }

    @Override
    protected void initMonsterData(Map<String, Module> moduleMap, ClientEnterDungeon enterFight, Object obj) {
        ClientEnterSkyTower enterFightPacket = (ClientEnterSkyTower) enterFight;
        super.initMonsterData(moduleMap, enterFightPacket, obj);
        //根据场景类型判断是否要发送波数的全部怪物类型信息;
        StageinfoVo stageVo = (StageinfoVo) obj;
        MonsterSpawnVo firstWave = null;
        /* 怪物数据 */
        for (int monsterSpawnId : stageVo.getMonsterSpawnIdList()) {
            MonsterSpawnVo monsterSpawnVo = SceneManager.getMonsterSpawnVo(monsterSpawnId);
            //如果找到无条件的话，认为他是第一波;
            if (monsterSpawnVo.getSpawnType() == 1) {
                firstWave = monsterSpawnVo;
                break;
            }
        }
        if (firstWave != null) {
            List<Integer> recordSpawnIdList = new ArrayList<>();
            List<String> dataList = new ArrayList<>();
            int tmpSpawnId = firstWave.getMonsterSpawnId();
            MonsterSpawnVo tmpSpawnVo = null;
            String tmpStr = null;
            do {
                tmpSpawnVo = SceneManager.getMonsterSpawnVo(tmpSpawnId);
                tmpStr = tmpSpawnVo.getMonsterTypeCountStr();
                //判断是否有重复的，如果有就说明会造成死循环;
                if (recordSpawnIdList.contains(tmpSpawnId)) {
                    com.stars.util.LogUtil.error("镇妖塔的刷怪数据配置有问题,Spawn nextSpawnId不能相互指向, 会造成死循环问题！");
                    break;
                }
                recordSpawnIdList.add(tmpSpawnId);
                dataList.add(tmpStr);
                tmpSpawnId = tmpSpawnVo.getNextSpawnId();
            } while (tmpSpawnId > 0);
            enterFightPacket.setWaveMonsterTypeList(dataList);
        } else {
            LogUtil.error("镇妖塔的刷怪数据配置有问题,找不到第一波怪的数据!");
        }
    }

}
