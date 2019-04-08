package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.modules.familyactivities.invade.FamilyInvadeManager;
import com.stars.modules.familyactivities.invade.event.FamilyInvadeDungeonFinishEvent;
import com.stars.modules.familyactivities.invade.event.FamilyInvadeEnterDungeonEvent;
import com.stars.modules.familyactivities.invade.prodata.FamilyInvadeVo;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFamilyInvade;
import com.stars.modules.scene.prodata.MonsterAttributeVo;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/10/19.
 */
public class FamilyActInvadeScene extends TeamDungeonScene {
    private int invadeId;// voId
    public int monsterNpcUId;// 触发战斗monsterNpcUId
    private long familyId;// 家族Id
    public int teamId;// 队伍Id

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object vo) {
        FamilyInvadeVo invadeVo = (FamilyInvadeVo) vo;
        StageinfoVo stageinfoVo = SceneManager.getStageVo(invadeVo.getStageId());
        if (stageinfoVo == null)
            return false;
        this.invadeId = invadeVo.getInvadeId();
        this.stageId = stageinfoVo.getStageId();
        this.setSceneType(stageinfoVo.getStageType());
        return true;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object monsterNpcUId) {
        this.monsterNpcUId = (int) monsterNpcUId;
        FamilyInvadeVo invadeVo = FamilyInvadeManager.getInvadeVo(invadeId);
        StageinfoVo stageVo = SceneManager.getStageVo(invadeVo.getStageId());
        this.startTimestamp = System.currentTimeMillis();
        this.stageStatus = SceneManager.STAGE_PROCEEDING;
        ClientEnterFamilyInvade enterPacket = new ClientEnterFamilyInvade();
        enterPacket.setStageId(stageId);
        enterPacket.setFightType(stageVo.getStageType());
        /* 初始化怪物 */
        initMonsterData(moduleMap, enterPacket, stageVo);
        enterPacket.setFighterEntityList(entityMap.values());
        /* 动态阻挡数据 */
        initDynamicBlockData(enterPacket, stageVo);
        sendPacketToTeamMembers(enterPacket, -1);
        sendEventToTeamMembers(new FamilyInvadeEnterDungeonEvent(stageId), -1);
    }

    @Override
    protected void selfDead(List<String> uIdList) {
        for (String uniqueId : uIdList) {
            this.deadTimeMap.put(uniqueId, System.currentTimeMillis());
        }
        boolean isAllDead = true;
        for (long memberRoleId : memberRoleIds) {
            if (!deadTimeMap.containsKey(String.valueOf(memberRoleId))) {
                isAllDead = false;
                break;
            }
        }
        if (isAllDead) {
            // 队员全部死亡
            stageStatus = SceneManager.STAGE_FAIL;
            finishDeal(null, stageStatus);
        }
    }

    @Override
    public void finishDeal(Map<String, Module> moduleMap, byte finish) {
        this.endTimestamp = System.currentTimeMillis();
        FamilyInvadeDungeonFinishEvent event = new FamilyInvadeDungeonFinishEvent(invadeId, finish);
        sendEventToTeamMembers(event, -1);
        ServiceHelper.familyActInvadeService().challengeFinish(familyId, monsterNpcUId, finish, damageMap, teamId);
    }

    @Override
    public Map<String, FighterEntity> spawnMonster(Map<String, Module> moduleMap, int monsterSpawnId) {
        Map<String, FighterEntity> resultMap = new HashMap<>();
        spawnSeq++;
        spawnMapping.put(getSpawnUId(monsterSpawnId), new LinkedList<String>());
        notTrapMonsterMap.put(getSpawnUId(monsterSpawnId), new LinkedList<String>());
        MonsterSpawnVo monsterSpawnVo = SceneManager.getMonsterSpawnVo(monsterSpawnId);
        if (monsterSpawnVo == null) {
            LogUtil.error("找不到刷怪组配置monsterspawnid={},请检查表", monsterSpawnId, new IllegalArgumentException());
            return resultMap;
        }
        int index = 0;
        for (MonsterAttributeVo monsterAttrVo : monsterSpawnVo.getMonsterAttrList()) {
            String monsterUniqueId = getMonsterUId(stageId, monsterSpawnId, monsterAttrVo.getStageMonsterId());
            FighterEntity monsterEntity = FighterCreator.create(FighterEntity.TYPE_MONSTER, monsterUniqueId,
                    getSpawnUId(monsterSpawnId), monsterSpawnId, monsterAttrVo, monsterSpawnVo.getAwake(),
                    monsterSpawnVo.getSpawnDelayByIndex(index++), null);
            entityMap.put(monsterUniqueId, monsterEntity);
            resultMap.put(monsterUniqueId, monsterEntity);

            spawnMapping.get(getSpawnUId(monsterSpawnId)).add(monsterUniqueId);
            // 不是陷阱怪物
            if (monsterAttrVo.getIsTrap() == 0) {
                notTrapMonsterMap.get(getSpawnUId(monsterSpawnId)).add(monsterUniqueId);
            }else if (monsterAttrVo.getIsTrap() == 1) {//是陷阱怪
            	//添加到陷阱的怪的集合
                trapMonsterMap.put(monsterUniqueId, monsterAttrVo.getStageMonsterId());
			}
        }
        return resultMap;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }
}
