package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterDungeon;
import com.stars.modules.scene.prodata.StageinfoVo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by panzhenfeng on 2016/9/6.
 */
public class GameCaveScene extends FightScene {

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object layerId) {
        return true;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object obj) {
        int stageId = (int)obj;
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        this.stageId = stageId;
        this.startTimestamp = System.currentTimeMillis();
        this.stageStatus = SceneManager.STAGE_PROCEEDING;
        ClientEnterDungeon enterFight = new ClientEnterDungeon();
        requestSendClientEnterFight(moduleMap, enterFight, stageVo);
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        // 先发剧情播放记录
        sceneModule.sendPlayedDrama(this, stageId);
        sceneModule.send(enterFight);
    }

    @Override
    public void exit(Map<String, Module> moduleMap) {

    }

    @Override
    public boolean isEnd() {
        return false;
    }

    /**
     * 请求发送响应请求战斗协议;
     *
     * @param stageVo       场景vo数据
     */
    protected void requestSendClientEnterFight(Map<String, Module> moduleMap, ClientEnterDungeon enterFight, StageinfoVo stageVo) {
        enterFight.setIsAgain(isAgain);
        enterFight.setStageId(stageId);
        enterFight.setFightType(SceneManager.getStageVo(stageId).getStageType());
        List<FighterEntity> fighterList = new LinkedList<>();
        /* 出战角色 */
        fighterList.add(FighterCreator.createSelf(moduleMap, stageVo.getPosition(), stageVo.getRotation()));
        /* 出战伙伴 */
        BuddyModule buddyModule = (BuddyModule) moduleMap.get(MConst.Buddy);
        // 有出战伙伴
        if (buddyModule.getFightBuddyId() != 0) {
            fighterList.add(FighterCreator.create(FighterEntity.TYPE_BUDDY, FighterEntity.CAMP_SELF,
                    buddyModule.getRoleBuddy(buddyModule.getFightBuddyId())));
        }
        /* 预加载怪物 */
        initMonsterData(moduleMap, enterFight, stageVo);
        fighterList.addAll(entityMap.values());
        enterFight.setFighterEntityList(fighterList);
        /* 动态阻挡数据 */
        initDynamicBlockData(enterFight, stageVo);
    }

    @Override
    public void finishDeal(Map<String, Module> moduleMap, byte finish) {

    }

    @Override
    public void enemyDead(Map<String, Module> moduleMap, List<String> uIdList) {

    }

    @Override
    public void selfDead(Map<String, Module> moduleMap) {

    }

    @Override
    public void areaSpawnMonster(Map<String, Module> moduleMap, int spawnId) {

    }
}
