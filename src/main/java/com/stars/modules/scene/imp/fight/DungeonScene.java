package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.dungeon.DungeonManager;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.prodata.DungeoninfoVo;
import com.stars.modules.dungeon.prodata.WorldinfoVo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.event.PassStageEvent;
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
import com.stars.modules.tool.ToolModule;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;

import java.util.*;

/**
 * 关卡战斗场景
 * Created by liuyuheng on 2016/7/6.
 * 诗歌关卡的scene，是由继承DungeonScene的PoemScene重载
 */
public class DungeonScene extends FightScene {
    protected int dungeonId;// 关卡Id

    public int getDungeonId() {
        return dungeonId;
    }

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object dungeonId) {
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        int tmpDungeonId = (int) dungeonId;
        if (tmpDungeonId == sceneModule.getGmDungeonId()) return true;
        boolean isCanDo = false;
        DungeoninfoVo dungeonVo = DungeonManager.getDungeonVo(tmpDungeonId);
        if (dungeonVo == null) {
            return false;
        }
        StageinfoVo stageVo = SceneManager.getStageVo(dungeonVo.getStageId());
        if (stageVo == null) {
            return false;
        }
        // 检查挑战次数
        DungeonModule dungeonModule = (DungeonModule) moduleMap.get(MConst.Dungeon);
        if (!dungeonModule.countCheck(dungeonVo.getDungeonId())) {
            return false;
        }
        WorldinfoVo chapterVo = DungeonManager.getChapterVo(dungeonVo.getWorldId());
        ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
        switch (chapterVo.getSort()) {
            case 1: {
                // 扣除挑战消耗
                if (toolModule.deleteAndSend(dungeonVo.getEnterCostMap(), EventType.DUNGEONSCENE.getCode())) {
                    isCanDo = true;
                }
            }
            break;
            case 2: {
                /**
                 * 英雄副本胜利扣除体力，进入前只检测就可以了
                 *
                 */
                if (toolModule.contains(dungeonVo.getEnterCostMap())) {
                    isCanDo = true;
                }
            }
            break;
        }
        if (!isCanDo) {
            sceneModule.warn("条件不足,不能进入关卡");
        }
        return isCanDo;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object dungeonId) {
        int tmpDungeonId = (int) dungeonId;
        //这里进来说明已经通过了canEnter的测试，所以不再判空了;
        DungeoninfoVo dungeonVo = DungeonManager.getDungeonVo(tmpDungeonId);
        StageinfoVo stageVo = SceneManager.getStageVo(dungeonVo.getStageId());
        this.stageId = dungeonVo.getStageId();
        this.startTimestamp = System.currentTimeMillis();
        this.stageStatus = SceneManager.STAGE_PROCEEDING;
        this.dungeonId = dungeonVo.getDungeonId();
        DungeonModule dungeonModule = (DungeonModule) moduleMap.get(MConst.Dungeon);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        if (sceneModule.getGmDungeonId() != tmpDungeonId) {
            dungeonModule.addEnterCount(tmpDungeonId);
        }
        sceneModule.setGmDungeonId(0);
        ClientEnterDungeon enterFight = new ClientEnterDungeon();
        enterFight.setDungeonId(tmpDungeonId);
         /* 副本失败时间 */
        if (stageVo.getFailConMap().containsKey(SceneManager.FAIL_CONDITION_TIME)) {
            enterFight.setFailTime(stageVo.getFailConMap().get(SceneManager.FAIL_CONDITION_TIME));
        }
        requestSendClientEnterFight(moduleMap, enterFight, stageVo);

        // 先发剧情播放记录
        sceneModule.sendPlayedDrama(this, stageId);
        sceneModule.send(enterFight);
//        sceneModule.cacheSend(enterFight);
        //日志
        String goType = "1";
        int juci = dungeonModule.getDungeonCount(tmpDungeonId);
        if (dungeonModule.isFirstPass(tmpDungeonId)) {
            goType = "1";
        } else {
            goType = "2";
        }
        ServerLogModule log = (ServerLogModule) moduleMap.get(MConst.ServerLog);
        log.Log_core_case(ThemeType.DUNGEON_START.getOperateId(), ThemeType.DUNGEON_START.getOperateName(), "enter", juci + "", tmpDungeonId + "", "", goType, dungeonVo.getBossIcon() + 1);
    }

    @Override
    public void updateTimeExecute(Map<String, Module> moduleMap) {

        StageinfoVo stageVo = SceneManager.getStageVo(this.stageId);
        if (stageVo == null) return;
        resendPacket(moduleMap);
        // 新手关卡不做时间胜利失败检测
        if (getSceneType() == SceneManager.SCENETYPE_DUNGEON && stageVo.getNewcomer() == 1) {
            return;
        }
        if (this.stageStatus == SceneManager.STAGE_PROCEEDING && stageVo.containTimeCondition()) {
            // 胜利失败检测
            checkFinish(null);
            if (stageStatus != SceneManager.STAGE_PROCEEDING) {
                finishDeal(moduleMap, stageStatus);
            }
        }
    }

    /**
     * 请求发送响应请求战斗协议;
     *
     * @param stageVo 场景vo数据
     */
    protected void requestSendClientEnterFight(Map<String, Module> moduleMap, ClientEnterDungeon enterFight,
                                               StageinfoVo stageVo) {
        enterFight.setIsAgain(isAgain);
        enterFight.setStageId(stageId);
        enterFight.setFightType(SceneManager.getStageVo(stageId).getStageType());
        List<FighterEntity> fighterList = new LinkedList<>();
        /* 出战角色 */
        FighterEntity roleEntity = FighterCreator.createSelf(moduleMap, stageVo.getPosition(), stageVo.getRotation());
        com.stars.util.LogUtil.info("roleSkill:{}", roleEntity.getSkills());
        entityMap.put(roleEntity.getUniqueId(), roleEntity);
        /* 出战伙伴 */
        /* 预加载怪物 */
        initMonsterData(moduleMap, enterFight, stageVo);
        fighterList.addAll(entityMap.values());
        enterFight.setFighterEntityList(fighterList);
        /* 动态阻挡数据 */
        initDynamicBlockData(enterFight, stageVo);
        /* 是否自动战斗 */
        RoleModule rm = (RoleModule) moduleMap.get(MConst.Role);
        enterFight.setAutoFlag(rm.getAutoFightFlag(enterFight.getFightType()));

    }

    @Override
    public void exit(Map<String, Module> moduleMap) {
        if (stageStatus == SceneManager.STAGE_PROCEEDING || stageStatus == SceneManager.STAGE_PAUSE) {
            ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
            toolModule.addAndSend(totalDropMap, EventType.DUNGEONSCENE.getCode());
        }
    }

    @Override
    public boolean isEnd() {
        return false;
    }

    @Override
    public void enemyDead(Map<String, Module> moduleMap, List<String> uIdList) {
        // 刷怪数据
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
            com.stars.util.MapUtil.add(totalDropMap, monsterEntity.getDropMap());
        }
        StageinfoVo stageVo = SceneManager.getStageVo(this.stageId);
        // 胜利失败检测
        if (stageStatus == SceneManager.STAGE_PROCEEDING || stageStatus == SceneManager.STAGE_PAUSE) {
            checkFinish(uIdList, stageVo.getNewcomer() == 1);
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

    public void checkFinish(List<String> uIdList, boolean isNewComer) {
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        // 先检测胜利
        for (Map.Entry<Byte, Integer> entry : stageVo.getVictoryConMap().entrySet()) {
            switch (entry.getKey()) {
                case SceneManager.VICTORY_CONDITION_KILL_ALL:// 全部击杀
                    killAll();
                    break;
                case SceneManager.VICTORY_CONDITION_KILL_APPOINT:// 击杀指定
                    killAppoint(SceneManager.STAGE_VICTORY, entry.getValue(), uIdList);
                    break;
                case SceneManager.VICTORY_CONDITION_TIME:// 指定时间后
                    victoryCheckTime(entry.getValue());
                    break;
                case SceneManager.VICTORY_CONDITION_KILL_BOSS:// 击杀boss类型怪物
                    killBoss(uIdList);
                    break;
                default:
                    break;
            }
        }
        // 失败
        if (!isNewComer) {
            for (Map.Entry<Byte, Integer> entry : stageVo.getFailConMap().entrySet()) {
                switch (entry.getKey()) {
                    case SceneManager.FAIL_CONDITION_SELFDEAD:
                        break;
                    case SceneManager.FAIL_CONDITION_TIME:// 指定时间后
                        defeatCheckTime(entry.getValue());
                        break;
                    case SceneManager.FAIL_CONDITION_KILL_APPOINT:// 击杀指定 失败
                        killAppoint(SceneManager.STAGE_FAIL, entry.getValue(), uIdList);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void selfDead(Map<String, Module> moduleMap) {
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        updateCurHp(roleModule.id(), 0);
        super.selfDead(moduleMap);
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
    public void finishDeal(Map<String, Module> moduleMap, byte finish) {
        this.endTimestamp = System.currentTimeMillis();
        // 关卡掉落
        Map<Integer, Integer> rewardMap = totalDropMap;
        DungeonModule dungeonModule = (DungeonModule) moduleMap.get(MConst.Dungeon);
        byte star = 0;
        int juci = dungeonModule.getDungeonCount(dungeonId);
        DungeoninfoVo dungeonVo = DungeonManager.getDungeonVo(dungeonId);
        StringBuffer info = new StringBuffer();
        // 胜利通关
        String goType = "1";
        boolean isFirstPass = false;
        if (finish == SceneManager.STAGE_VICTORY) {
            WorldinfoVo chapterVo = DungeonManager.getChapterVo(dungeonVo.getWorldId());
            if (chapterVo.getSort() == 2) {
                ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
                toolModule.deleteAndSend(dungeonVo.getEnterCostMap(), EventType.DUNGEONSCENE.getCode());
            }
            if (dungeonModule.isFirstPass(dungeonId)) {
                goType = "1";
                isFirstPass = true;
            } else {
                goType = "2";
                isFirstPass = false;
            }
            // 评星
            star = calStar(dungeonModule.id());
            // 通关奖励+首通奖励
            MapUtil.add(rewardMap, dungeonModule.getPassReward(dungeonId));
            dungeonModule.passDungeon(dungeonId, star);
            info.append("fight_time:");
            info.append((this.endTimestamp - this.startTimestamp) / 1000).append("#sp_case:");
            info.append(dungeonModule.getRoleMaxDungeonId((byte) 0)).append("#nm_case:").append(dungeonModule.getRoleMaxDungeonId((byte) 1));
            //日志
            ServerLogModule log = (ServerLogModule) moduleMap.get(MConst.ServerLog);
            log.Log_core_case(ThemeType.DUNGEON_WIN.getOperateId(), ThemeType.DUNGEON_WIN.getOperateName(), "1", juci + "", dungeonId + "", info.toString(), goType, dungeonVo.getBossIcon() + 1);
        } else if (finish == SceneManager.STAGE_FAIL) {//挑战失败
            if (dungeonModule.isFirstPass(dungeonId)) {
                goType = "1";
            } else {
                goType = "2";
            }
            info.append("fight_time:");
            info.append((this.endTimestamp - this.startTimestamp) / 1000).append("#sp_case:");
            info.append(dungeonModule.getRoleMaxDungeonId((byte) 0)).append("#nm_case:").append(dungeonModule.getRoleMaxDungeonId((byte) 1));
            ServerLogModule log = (ServerLogModule) moduleMap.get(MConst.ServerLog);
            log.Log_core_case(ThemeType.DUNGEON_FAIL.getOperateId(), ThemeType.DUNGEON_FAIL.getOperateName(), "1", juci + "", dungeonId + "", info.toString(), goType, dungeonVo.getBossIcon() + 1);
        }
        ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
        Map<Integer, Integer> map = toolModule.addAndSend(rewardMap, EventType.DUNGEONSCENE.getCode());
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        int jobId = roleModule.getRoleRow().getJobId();
        /**
         * 英雄关卡一些首通道具可能角色已经拥有而不会在结算显示，应策划要求
         * 全部显示出来
         */
        if (!DungeonManager.isHeroStage(dungeonId)) {
            // 自动使用宝箱类型物品合并显示
            switchBoxTool(map, jobId);
        }
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        ClientStageFinish clientStageFinish = new ClientStageFinish(SceneManager.SCENETYPE_DUNGEON, finish);
        clientStageFinish.setStar(star);
        clientStageFinish.setItemMap(map);
        clientStageFinish.setUseTime((int) Math.floor((endTimestamp - startTimestamp) / 1000.0));
        sceneModule.send(clientStageFinish);
        if (finish == SceneManager.STAGE_VICTORY) {
            sceneModule.dispatchEvent(new PassStageEvent(this.dungeonId, star, isFirstPass));
        }
    }

    public void updateCurHp(long roleId, int curHp) {
        FighterEntity fighterEntity = entityMap.get("" + roleId);
        if (fighterEntity != null) {
            int maxHp = fighterEntity.getAttribute().getMaxhp();
            if (curHp > maxHp) return;
            if (curHp < 0) curHp = 0;

            fighterEntity.getAttribute().setHp(curHp);
        }
    }

    /**
     * 评星计算
     *
     * @return
     */
    protected byte calStar(long roleId) {
        DungeoninfoVo dungeonVo = DungeonManager.getDungeonVo(dungeonId);
        byte star = 0;

        int usedTime = (int) Math.floor((endTimestamp - startTimestamp) / 1000.0);

        int leftHp = 0;
        int maxHp = 0;
        FighterEntity fighterEntity = entityMap.get("" + roleId);
        if (fighterEntity != null) {
            leftHp = fighterEntity.getAttribute().getHp();
            maxHp = fighterEntity.getAttribute().getMaxhp();
        }
        float persent = 0;
        if (maxHp > 0) {
            persent = (float) leftHp / (float) maxHp * 100;
        }

        List<List<Integer>> starConList = dungeonVo.getStarConList();
        int size = starConList.size();
        for (int i = 0; i < size; i++) {
            List<Integer> unit = starConList.get(i);
            if (unit.size() < 2) continue;
            byte condType = (byte) ((int) unit.get(0));
            int param = unit.get(1);
            boolean isFit = false;
            switch (condType) {
                case DungeoninfoVo.STAR_COND_TIME:
                    if (usedTime <= param) {
                        isFit = true;
                    }
                    break;
                case DungeoninfoVo.STAR_COND_HP:
                    if (persent >= (float) param) {
                        isFit = true;
                    }
                    break;
                default:
                    break;
            }

            if (isFit) {
                star++;
                if (i >= size - 1) {//如果最后一个星位满足条件，则满星
                    star = (byte) size;
                    return star;
                }
            }
        }
        return star;
    }
}
