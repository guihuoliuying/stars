package com.stars.modules.task;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.authentic.event.AuthenticEvent;
import com.stars.modules.buddy.event.BuddyUpgradeEvent;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.familyEscort.event.FamilyEscortFightEvent;
import com.stars.modules.familyEscort.event.FamilyEscortKillEvent;
import com.stars.modules.gm.GmManager;
import com.stars.modules.newequipment.event.EquipStrengthChangeEvent;
import com.stars.modules.newequipment.event.EquipWashChangeEvent;
import com.stars.modules.newofflinepvp.event.OfflinePvpClientStageFinishEvent;
import com.stars.modules.ride.event.RideLevelUpEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.scene.event.PassBraveStageEvent;
import com.stars.modules.scene.event.PassStageEvent;
import com.stars.modules.scene.event.TalkWithNpcEvent;
import com.stars.modules.soul.event.SoulLevelUpEvent;
import com.stars.modules.soul.event.SoulStageUpEvent;
import com.stars.modules.task.gm.TaskGmHandler;
import com.stars.modules.task.listener.*;
import com.stars.modules.task.prodata.TaskVo;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

public class TaskModuleFactory extends AbstractModuleFactory<TaskModule> {

    public TaskModuleFactory() {
        super(new TaskPacketSet());
    }

    @Override
    public TaskModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new TaskModule(id, self, eventDispatcher, map);
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("dotask", new TaskGmHandler());
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(AddToolEvent.class, new GetToolTaskListener(module));
        eventDispatcher.reg(RoleLevelUpEvent.class, new LevelUpListener(module));
        eventDispatcher.reg(PassStageEvent.class, new PassGuanKaTaskListener(module));
        eventDispatcher.reg(PassBraveStageEvent.class, new PassBraveStageListener(module));
        eventDispatcher.reg(TalkWithNpcEvent.class, new TalkWithNpcListener(module));

        BranchTaskListener branchTaskListener = new BranchTaskListener((TaskModule) module);
        eventDispatcher.reg(DailyFuntionEvent.class, branchTaskListener);
//		eventDispatcher.reg(OfflinePvpVictoryEvent.class, branchTaskListener);
        eventDispatcher.reg(OfflinePvpClientStageFinishEvent.class, branchTaskListener);
        eventDispatcher.reg(EquipWashChangeEvent.class, branchTaskListener);
        eventDispatcher.reg(RideLevelUpEvent.class, branchTaskListener);
        eventDispatcher.reg(BuddyUpgradeEvent.class, branchTaskListener);
        eventDispatcher.reg(EquipStrengthChangeEvent.class, branchTaskListener);
        eventDispatcher.reg(AuthenticEvent.class, branchTaskListener);
        eventDispatcher.reg(SoulLevelUpEvent.class, branchTaskListener);
        eventDispatcher.reg(SoulStageUpEvent.class, branchTaskListener);

        FamilyEscortKillListener familyEscortKillListener = new FamilyEscortKillListener((TaskModule) module);
        eventDispatcher.reg(FamilyEscortKillEvent.class, familyEscortKillListener);
        eventDispatcher.reg(FamilyEscortFightEvent.class, familyEscortKillListener);


    }

    @Override
    public void loadProductData() throws Exception {
        loadTaskVoFromDb();
    }


    private boolean loadTaskVoFromDb() {
        String sql = "select * from mission";
        try {
            TaskManager.setTaskVoMap((HashMap<Integer, TaskVo>) DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", TaskVo.class, sql));
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            return false;
        }
        return true;
    }
}
