package com.stars.modules.scene.imp.fight;

import com.stars.core.module.Module;
import com.stars.modules.scene.FightScene;

import java.util.List;
import java.util.Map;

/**
 * 野外夺宝场景PVE, 这个只是空壳(为了使用统一的进/出后的周围玩家处理)，真正的场景在PVELootTreasure里处理了;
 * Created by panzhenfeng on 2016/10/10.
 */
public class LootTreasureScenePVE extends FightScene {


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
