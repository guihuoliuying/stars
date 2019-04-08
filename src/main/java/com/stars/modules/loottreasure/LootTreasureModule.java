package com.stars.modules.loottreasure;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.arroundPlayer.ArroundPlayerModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.services.ServiceHelper;
import com.stars.util.ServerLogConst;

import java.util.Map;

/**
 * 野外夺宝模块;
 * Created by panzhenfeng on 2016/10/10.
 */
public class LootTreasureModule extends AbstractModule {
    public long startTime = System.currentTimeMillis();
    private boolean isInLootTreasure = false;

    public LootTreasureModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(MConst.LootTreasure, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onInit(boolean isCreation) {
        RoleModule roleModule = (RoleModule) this.module(MConst.Role);
        ServiceHelper.lootTreasureService().checkLootTreasureActivity(roleModule.getRoleRow().getRoleId());
    }


    //请求进入野外夺宝;
    public void requestAttend() {
        RoleModule roleModule = module(MConst.Role);
        FighterEntity myselfEntity = FighterCreator.createSelf(moduleMap());
        ServiceHelper.lootTreasureService().requestAttend(myselfEntity, roleModule.getRoleRow().getJobId());
        this.startTime = System.currentTimeMillis();
    }

    //通知已经回城了;
    public void noticeBackedCity() {
        //周围玩家的逻辑处理;
        ArroundPlayerModule apm = module("arroundplayer");
        SceneModule sceneModule = module("scene");
        RoleModule roleModule = module("role");
        StringBuilder builder = new StringBuilder("");
        builder.append(SceneManager.ARROUND_SCENE_PREFIX)
                .append(roleModule.getSafeStageId())
                .append("lootTreasure")
                .append(roleModule.getSafeStageId());
        apm.doEnterSceneEvent(SceneManager.SCENETYPE_CITY, builder.toString(), sceneModule.getLastSceneType(),
                roleModule.getJoinSceneStr());
        sceneModule.backToCity(false);
        ServerLogModule log = (ServerLogModule) module(MConst.ServerLog);
        log.Log_core_activity(ServerLogConst.ACTIVITY_WIN, ThemeType.ACTIVITY_101.getThemeId(), log.makeJuci(), ThemeType.ACTIVITY_101.getThemeId(), roleModule.getSafeStageId(), (System.currentTimeMillis() - this.startTime) / 1000);
//        sceneModule.backCityFromRemote(false);
    }

    //通知已经进入野外夺宝了;
    public void noticeEnteredLootTreasure(int stageId) {
        ArroundPlayerModule apm = module("arroundplayer");
        SceneModule sceneModule = module("scene");
        RoleModule roleModule = module("role");
        StringBuilder builder = new StringBuilder("");
        builder.append(SceneManager.ARROUND_SCENE_PREFIX)
                .append(roleModule.getSafeStageId())
                .append("lootTreasure")
                .append(stageId);
        apm.doEnterSceneEvent(SceneManager.SCENETYPE_LOOTTREASURE_PVE, builder.toString(), sceneModule.getLastSceneType(),
                roleModule.getJoinSceneStr());
    }

    public boolean isInLootTreasure() {
        return isInLootTreasure;
    }

    public void setInLootTreasure(boolean inLootTreasure) {
        isInLootTreasure = inLootTreasure;
    }
}
