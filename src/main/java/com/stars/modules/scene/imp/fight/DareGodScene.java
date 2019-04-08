package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.fightdata.Damage;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientSpawnMonster;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterDareGod;
import com.stars.modules.scene.packet.fightSync.ServerFightDamage;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.ServerLogConst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-08-24.
 */
public class DareGodScene extends FightScene {
    private long damageValue;
    private int monterIds;
    private int fightType;

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object obj) {
        com.stars.util.LogUtil.info("挑战女神|canEnter");
        String tmp = (String) obj;
        String[] tmp1 = tmp.split("\\+");
        int tmpStageId = Integer.parseInt(tmp1[0]);
        StageinfoVo stageVo = SceneManager.getStageVo(tmpStageId);
        if (stageVo == null) {
            return false;
        }
        return true;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object obj) {
        LogUtil.info("挑战女神|enter");
        String tmp = (String) obj;
        String[] tmp1 = tmp.split("\\+");
        int tmpStageId = Integer.parseInt(tmp1[0]);
        this.fightType = Integer.parseInt(tmp1[1]);
        this.monterIds = Integer.parseInt(tmp1[2]);
        this.stageId = tmpStageId;
        this.startTimestamp = System.currentTimeMillis();
        this.stageStatus = SceneManager.STAGE_PROCEEDING;
        ClientEnterDareGod dareGod = new ClientEnterDareGod();
        StageinfoVo stageVo = SceneManager.getStageVo(tmpStageId);
        requestSendClientEnterFight(moduleMap, dareGod, stageVo);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        // 先发剧情播放记录
        sceneModule.sendPlayedDrama(this, this.stageId);
        sceneModule.send(dareGod);
        MainRpcHelper.dareGodService().delFightTime(MultiServerHelper.getChatServerId(), MultiServerHelper.getServerId(), sceneModule.id(), 1);
        ServerLogModule logModule = (ServerLogModule) moduleMap.get(MConst.ServerLog);
        logModule.Log_core_activity(ServerLogConst.ACTIVITY_START, ThemeType.ACTIVITY_46.getThemeId(), logModule.makeJuci(),
                ThemeType.ACTIVITY_46.getThemeId(), tmpStageId, 0);
    }

    /**
     * @param moduleMap
     * @param enterFight
     * @param stageVo
     */
    protected void requestSendClientEnterFight(Map<String, Module> moduleMap, ClientEnterDareGod enterFight,
                                               StageinfoVo stageVo) {
        enterFight.setIsAgain(isAgain);
        enterFight.setStageId(stageId);
        enterFight.setFightType(stageVo.getStageType());
        if (stageVo.containTimeCondition()) {
            enterFight.setVitoryTimes(stageVo.getVictoryConMap().get(SceneManager.VICTORY_CONDITION_TIME));
        }
        /* 出战角色 */
        FighterEntity roleEntity = FighterCreator.createSelf(moduleMap, stageVo.getPosition(), stageVo.getRotation());
        entityMap.put(roleEntity.getUniqueId(), roleEntity);
        /* 出战伙伴 */
        BuddyModule buddyModule = (BuddyModule) moduleMap.get(MConst.Buddy);
        // 有出战伙伴
        if (buddyModule.getFightBuddyId() != 0) {
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
    }

    @Override
    public void exit(Map<String, Module> moduleMap) {
        endTimestamp = System.currentTimeMillis();
        if (stageStatus == SceneManager.STAGE_PROCEEDING || stageStatus == SceneManager.STAGE_PAUSE) {
            // TODO: 2017-08-24 退出操作
            ServerLogModule logModule = (ServerLogModule) moduleMap.get(MConst.ServerLog);
            MainRpcHelper.dareGodService().dealExitOrFinishScene(MultiServerHelper.getChatServerId(), MultiServerHelper.getServerId(),
                    logModule.id(), null, damageValue, fightType);
            logModule.Log_core_activity(ServerLogConst.ACTIVITY_FAIL, ThemeType.ACTIVITY_46.getThemeId(), logModule.makeJuci(),
                    ThemeType.ACTIVITY_46.getThemeId(), stageId,
                    (endTimestamp - startTimestamp) / 1000);
        }
    }

    @Override
    public boolean isEnd() {
        return false;
    }

    @Override
    public void finishDeal(Map<String, Module> moduleMap, byte finish) {
        this.endTimestamp = System.currentTimeMillis();
        ServerLogModule logModule = (ServerLogModule) moduleMap.get(MConst.ServerLog);
        ClientStageFinish csf = new ClientStageFinish(SceneManager.SCENETYPE_DARE_GOD, finish);
        MainRpcHelper.dareGodService().dealExitOrFinishScene(MultiServerHelper.getChatServerId(), MultiServerHelper.getServerId(),
                logModule.id(), csf, damageValue, fightType);
        byte logType = finish == SceneManager.STAGE_VICTORY ? ServerLogConst.ACTIVITY_WIN : ServerLogConst.ACTIVITY_FAIL;
        logModule.Log_core_activity(logType, ThemeType.ACTIVITY_46.getThemeId(), logModule.makeJuci(),
                ThemeType.ACTIVITY_46.getThemeId(), stageId,
                (endTimestamp - startTimestamp) / 1000);
    }

    @Override
    public void receivePacket(Map<String, Module> moduleMap, PlayerPacket packet) {
        if (packet instanceof ServerFightDamage) {
            dealFightDamage(((ServerFightDamage) packet).getDamageList());
        }
    }

    /**
     * 处理伤害
     *
     * @param list
     */
    private void dealFightDamage(List<Damage> list) {
        for (Damage damage : list) {
            if (!entityMap.containsKey(damage.getReceiverId()))
                continue;
            FighterEntity monsterEntity = entityMap.get(damage.getReceiverId());
            if (monsterEntity == null || monterIds != monsterEntity.getMonsterAttrId())
                continue;
            // todo:伤害验证
            if (damage.getValue() > 0) {
                continue;
            }
            damageValue = damageValue + damage.getValue() * -1;
        }
    }

    @Override
    public void enemyDead(Map<String, Module> moduleMap, List<String> uIdList) {
// 刷怪数据
        Map<String, FighterEntity> sendMonsterMap = new HashMap<>();
        // 销毁陷阱怪数据
        List<String> destroyTrapMonsterList = new ArrayList<>();
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
//        // 胜利失败检测
//        if (stageStatus == SceneManager.STAGE_PROCEEDING || stageStatus == SceneManager.STAGE_PAUSE) {
//            checkFinish(uIdList);
//            if (stageStatus != SceneManager.STAGE_PROCEEDING && stageStatus != SceneManager.STAGE_PAUSE) {
//                finishDeal(moduleMap, stageStatus);
//            }
//        }
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
}
