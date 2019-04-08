package com.stars.modules.daregod;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.fashion.FashionModule;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.vip.VipModule;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-08-23.
 */
public class DareGodModule extends AbstractModule {
    public DareGodModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        doOnline();
    }

    @Override
    public void onReconnect() throws Throwable {
        doOnline();
    }

    private void doOnline() {
        ForeShowModule open = module(MConst.ForeShow);
        if (!open.isOpen(ForeShowConst.DareGod)) {
            com.stars.util.LogUtil.info("doOnline|{} 尚未开放 roleId:{}", ForeShowConst.DareGod, id());
            return;
        }
        RoleModule role = module(MConst.Role);
        FashionModule fashion = module(MConst.Fashion);
        try {
            MainRpcHelper.dareGodService().onLine(MultiServerHelper.getChatServerId(),
                    MultiServerHelper.getServerId(), id(), role.getRoleRow().getName(), fashion.getDressFashionId(), role.getFightScore(), role.getRoleRow().getJobId());
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public void view() {
        ForeShowModule open = module(MConst.ForeShow);
        if (!open.isOpen(ForeShowConst.DareGod)) {
            com.stars.util.LogUtil.info("view|{} 尚未开放 roleId:{}", ForeShowConst.DareGod, id());
            return;
        }
        RoleModule role = module(MConst.Role);
        VipModule vip = module(MConst.Vip);
        try {
            MainRpcHelper.dareGodService().view(MultiServerHelper.getChatServerId(), MultiServerHelper.getServerId(),
                    id(), role.getRoleRow().getName(), role.getFightScore(), role.getRoleRow().getJobId(), vip.getVipLevel());
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public void viewRank() {
        ForeShowModule open = module(MConst.ForeShow);
        if (!open.isOpen(ForeShowConst.DareGod)) {
            com.stars.util.LogUtil.info("viewRank|{} 尚未开放 roleId:{}", ForeShowConst.DareGod, id());
            return;
        }
        try {
            MainRpcHelper.dareGodService().viewRank(MultiServerHelper.getChatServerId(), MultiServerHelper.getServerId(), id());
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public void buyFightTime(int times) {
        ForeShowModule open = module(MConst.ForeShow);
        if (!open.isOpen(ForeShowConst.DareGod)) {
            com.stars.util.LogUtil.info("buyFightTime|{} 尚未开放 roleId:{}", ForeShowConst.DareGod, id());
            return;
        }
        ToolModule tool = module(MConst.Tool);
        if (tool.deleteAndSend(ToolManager.BANDGOLD, times * DareGodManager.BUY_TIMES_REQ_ITEM_COUNT, EventType.DARE_GOD.getCode())) {
            VipModule vip = module(MConst.Vip);
            try {
                MainRpcHelper.dareGodService().buyTimes(MultiServerHelper.getChatServerId(), MultiServerHelper.getServerId(), id(), vip.getVipLevel(), times);
            } catch (Exception e) {
//                e.printStackTrace();
            }
        } else {
            warn("你没钱了!");
        }
    }

    public void addAward(Map<Integer, Integer> itemMap) {
        ToolModule tool = module(MConst.Tool);
        tool.addAndSend(itemMap, EventType.DARE_GOD.getCode());
    }

    public void enterFight(int stageId, int fightType, int monsterId) {
        SceneModule scene = module(MConst.Scene);
        scene.enterScene(SceneManager.SCENETYPE_DARE_GOD, stageId, stageId + "+" + fightType + "+" + monsterId);
        eventDispatcher().fire(new DailyFuntionEvent(DailyManager.DAILYID_DAREGOD, 1));
        LogUtil.info("挑战女神进入战斗|roleId:{},stageId:{},fightType:{},monsterId:{}", id(), stageId, fightType, monsterId);
    }

    public void changeFightScore(int newFightScore) {
        try {
            MainRpcHelper.dareGodService().updateFightScore(MultiServerHelper.getChatServerId(), id(), newFightScore);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public void changeName(String newName) {
        try {
            MainRpcHelper.dareGodService().updateRoleName(MultiServerHelper.getChatServerId(), id(), newName);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public void changeFashion(int newFashionId) {
        try {
            MainRpcHelper.dareGodService().updateFashionId(MultiServerHelper.getChatServerId(), id(), newFashionId);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public void changeJob(int newJobId) {
        try {
            MainRpcHelper.dareGodService().updateJobId(MultiServerHelper.getChatServerId(), id(), newJobId);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }
}
