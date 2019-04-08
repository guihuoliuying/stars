package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.scene.FightScene;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterPK;

import java.util.List;
import java.util.Map;

/**
 * Created by daiyaorong on 2016/9/2.
 */
public class PkScene extends FightScene {

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object obj) {
        return true;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object obj) {
    	ClientEnterPK enterPack = (ClientEnterPK)obj;
        SceneModule sceneModule = (SceneModule) moduleMap.get(MConst.Scene);
        sceneModule.send(enterPack);
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
}
