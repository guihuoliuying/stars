package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.familyactivities.treasure.FamilyTreasureManager;
import com.stars.modules.familyactivities.treasure.FamilyTreasureModule;
import com.stars.modules.familyactivities.treasure.prodata.FamilyAdventureVo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.fightdata.Damage;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientSpawnMonster;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFamilyTreasure;
import com.stars.modules.scene.packet.fightSync.ServerFightDamage;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.util.MapUtil;
import com.stars.util.ServerLogConst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017/2/10 14:10
 */
public class FamilyTreasureScene extends FightScene {

    private long damageValue;
    private int monsterAttrId;// boss属性Id
    private int thisLevel;//考虑到玩家在打boss关卡时，家族探宝的等级与步数可能已发生改变，所以备份一份数据
    private int thisStep;
    private String uniqueId;//唯一id;

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object stageId) {
        int tmpStageId = (int) stageId;
        StageinfoVo stageVo = SceneManager.getStageVo(tmpStageId);
        if (stageVo == null) {
            return false;
        }
        FamilyTreasureModule nftm = (FamilyTreasureModule) moduleMap.get(MConst.FamilyActTreasure);
        FamilyAdventureVo faVo = FamilyTreasureManager.getFamilyAdventureVoMap().get(nftm.getFamilyTreasureLevel()).get(nftm.getFamilyTreasureStep());
        this.monsterAttrId = faVo.getStageMonsterId();
        this.thisLevel = nftm.getFamilyTreasureLevel();
        this.thisStep = nftm.getFamilyTreasureStep();
        return true;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object stageId) {
        int tmpStageId = (int) stageId;
        this.stageId = tmpStageId;
        this.startTimestamp = System.currentTimeMillis();
        this.stageStatus = SceneManager.STAGE_PROCEEDING;
        FamilyTreasureModule nftm = (FamilyTreasureModule) moduleMap.get(MConst.FamilyActTreasure);
        nftm.dealAdventureCountInc();
        ClientEnterFamilyTreasure clientFight = new ClientEnterFamilyTreasure();
        StageinfoVo stageVo = SceneManager.getStageVo(tmpStageId);
        requestSendClientEnterFight(moduleMap, clientFight, stageVo);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        // 先发剧情播放记录
        sceneModule.sendPlayedDrama(this, this.stageId);
        sceneModule.send(clientFight);
        ServerLogModule logModule = (ServerLogModule) moduleMap.get(MConst.ServerLog);
        logModule.Log_core_activity(ServerLogConst.ACTIVITY_START, ThemeType.ACTIVITY_27.getThemeId(), logModule.makeJuci(),
                ThemeType.ACTIVITY_27.getThemeId(), tmpStageId, 0);
    }

    /**
     * @param moduleMap
     * @param enterFight
     * @param stageVo
     */
    protected void requestSendClientEnterFight(Map<String, Module> moduleMap, ClientEnterFamilyTreasure enterFight,
                                               StageinfoVo stageVo) {
        enterFight.setIsAgain(isAgain);
        enterFight.setStageId(stageId);
        enterFight.setFightType(stageVo.getStageType());
        Map<Integer, Integer> buffMap = getBuffdata(moduleMap);
        enterFight.addBuffData(buffMap);
        if (stageVo.containTimeCondition()) {
            enterFight.setVitoryTimes(stageVo.getVictoryConMap().get(SceneManager.VICTORY_CONDITION_TIME));
        }
        /* 出战角色 */
        FighterEntity roleEntity = FighterCreator.createSelf(moduleMap, stageVo.getPosition(), stageVo.getRotation());
        StringBuilder sb = new StringBuilder();
        if (!buffMap.isEmpty()) {
            sb.append("defaultbuff=");
            int count = 1;
            for (Map.Entry<Integer, Integer> entry : buffMap.entrySet()) {
                sb.append(entry.getKey()).append("+").append(entry.getValue()).append("+").append("-" + count);
                count++;
            }
        }
        roleEntity.addExtraValue(sb.toString());
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

    private Map<Integer, Integer> getBuffdata(Map<String, Module> moduleMap) {
        FamilyTreasureModule familyTreasureModule = (FamilyTreasureModule) moduleMap.get(MConst.FamilyActTreasure);
        return familyTreasureModule.addBuff();
    }

    @Override
    public void exit(Map<String, Module> moduleMap) {
        endTimestamp = System.currentTimeMillis();
        if (stageStatus == SceneManager.STAGE_PROCEEDING || stageStatus == SceneManager.STAGE_PAUSE) {
            FamilyTreasureModule ftm = (FamilyTreasureModule) moduleMap.get(MConst.FamilyActTreasure);
            ftm.dealFinishOrExitScene(null, thisLevel, thisStep, damageValue, monsterAttrId, monsterUniqueId);
            ServerLogModule logModule = (ServerLogModule) moduleMap.get(MConst.ServerLog);
            logModule.Log_core_activity(ServerLogConst.ACTIVITY_FAIL, ThemeType.ACTIVITY_27.getThemeId(), logModule.makeJuci(),
                    ThemeType.ACTIVITY_27.getThemeId(), stageId,
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
        FamilyTreasureModule ftm = (FamilyTreasureModule) moduleMap.get(MConst.FamilyActTreasure);
        //下发伤害
        ClientStageFinish csf = new ClientStageFinish(SceneManager.SCENETYPE_FAMILY_TREASURE, finish);
        ftm.dealFinishOrExitScene(csf, thisLevel, thisStep, damageValue, monsterAttrId, monsterUniqueId);
        ServerLogModule logModule = (ServerLogModule) moduleMap.get(MConst.ServerLog);
        byte logType = finish == SceneManager.STAGE_VICTORY ? ServerLogConst.ACTIVITY_WIN : ServerLogConst.ACTIVITY_FAIL;
        logModule.Log_core_activity(logType, ThemeType.ACTIVITY_27.getThemeId(), logModule.makeJuci(),
                ThemeType.ACTIVITY_27.getThemeId(), stageId,
                (endTimestamp - startTimestamp) / 1000);
    }

    @Override
    public void selfDead(Map<String, Module> moduleMap) {
        super.selfDead(moduleMap);
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
            if (monsterEntity == null || monsterEntity.getMonsterAttrId() != monsterAttrId)
                continue;
            // todo:伤害验证
            if (damage.getValue() > 0) {
                continue;
            }
            damageValue = damageValue + damage.getValue() * -1;
        }
    }
}
