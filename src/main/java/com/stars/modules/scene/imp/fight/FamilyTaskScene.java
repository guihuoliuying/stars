package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.event.PassBraveStageEvent;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientSpawnMonster;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterDungeon;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFamilyTask;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;

import java.util.*;

public class FamilyTaskScene extends FightScene {

	@Override
	public boolean canEnter(Map<String, Module> moduleMap, Object obj) {
		if(obj == null || "".equals(obj)) return false;
		int tmpStageId = (int) obj;
        StageinfoVo stageVo = SceneManager.getStageVo(tmpStageId);
        if (stageVo == null) {
            return false;
        }
        return true;
	}

	@Override
	public void enter(Map<String, Module> moduleMap, Object obj) {
		int tmpStageId = (int) obj;
        this.stageId = tmpStageId;
        this.startTimestamp = System.currentTimeMillis();
        this.stageStatus = SceneManager.STAGE_PROCEEDING;
        StageinfoVo stageVo = SceneManager.getStageVo(tmpStageId);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        ClientEnterFamilyTask enterPacket = new ClientEnterFamilyTask();
        requestSendClientEnterFight(moduleMap, enterPacket, stageVo);
        // 先发剧情播放记录
        sceneModule.sendPlayedDrama(this, stageId);
        sceneModule.send(enterPacket);
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
        enterFight.setFightType(stageVo.getStageType());
        List<FighterEntity> fighterList = new LinkedList<>();
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isEnd() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void finishDeal(Map<String, Module> moduleMap, byte finish) {
		SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
		// 关卡掉落
        Map<Integer, Integer> rewardMap = totalDropMap;
        ToolModule toolModule = (ToolModule) moduleMap.get(MConst.Tool);
        Map<Integer, Integer> map = toolModule.addAndSend(rewardMap, EventType.FAMILY_TASK_GUANKA.getCode());
        ClientStageFinish clientStageFinish = new ClientStageFinish(SceneManager.SCENETYPE_FAMILY_TASK, finish);
        clientStageFinish.setItemMap(map);
        clientStageFinish.setUseTime((int) Math.floor((endTimestamp - startTimestamp) / 1000.0));
        sceneModule.send(clientStageFinish);
        if (finish == SceneManager.STAGE_VICTORY) {
            sceneModule.dispatchEvent(new PassBraveStageEvent(this.stageId));
        }
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
            	LogUtil.info("monster dead but not get entity monsteruid="+monsterUId);
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
		// TODO Auto-generated method stub
		
	}

}
