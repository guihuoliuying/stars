package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.modules.scene.FightScene;

import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-05-09 9:30
 */
public class FamilyActWarScene extends FightScene {
    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object obj) {
        return true;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object obj) {

    }

    @Override
    public void exit(Map<String, Module> moduleMap) {

    }

    @Override
    public boolean isEnd() {
        return false;
    }

    @Override
    public void finishDeal(Map<String, Module> moduleMap, byte finish) {

    }

    @Override
    public void enemyDead(Map<String, Module> moduleMap, List<String> uIdList) {

    }

    @Override
    public void areaSpawnMonster(Map<String, Module> moduleMap, int spawnId) {

    }
}
