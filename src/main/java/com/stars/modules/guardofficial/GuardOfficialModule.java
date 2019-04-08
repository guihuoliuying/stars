package com.stars.modules.guardofficial;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.camp.CampModule;
import com.stars.modules.camp.event.MissionFinishEvent;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.prodata.ProduceDungeonVo;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.util.LogUtil;

import java.util.Calendar;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-07-12.
 */
public class GuardOfficialModule extends AbstractModule {

    public GuardOfficialModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {

    }

    /**
     * 下发场景信息
     */
    public void enterScene() {
        DungeonModule dungeonModule = module(MConst.Dungeon);
        dungeonModule.sendProduceDungeonVo(GuardOfficalManager.dungeonType);
        SceneModule scene = module(MConst.Scene);
        ProduceDungeonVo dungeonVo = dungeonModule.getEnterProduceDungeonVo(GuardOfficalManager.dungeonType);
        if (dungeonVo == null) {
            LogUtil.info("活动副本没有产品数据:{}", GuardOfficalManager.dungeonType);
            return;
        }
        scene.enterScene(SceneManager.SCENETYPE_GUARD_OFFICIAL, dungeonVo.getStageId(),
                GuardOfficalManager.dungeonType + "-" + dungeonVo.getStageId());
    }

    /**
     * 是否次数能够进入
     *
     * @return
     */
    public boolean canEnterScene() {
        CampModule camp = module(MConst.Camp);
        if (!camp.isMissionComplete(GuardOfficalManager.campMissionId)) {
            return true;
        } else {
            warn("该任务已完成");
            return false;
        }
    }

    public void finishEvent() {
        eventDispatcher().fire(new MissionFinishEvent(GuardOfficalManager.campMissionId));
    }
}
