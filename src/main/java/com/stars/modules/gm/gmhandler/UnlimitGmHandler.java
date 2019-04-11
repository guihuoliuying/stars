package com.stars.modules.gm.gmhandler;

import com.stars.core.module.Module;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.prodata.DailyVo;
import com.stars.modules.data.DataManager;
import com.stars.modules.dungeon.DungeonManager;
import com.stars.modules.dungeon.prodata.DungeoninfoVo;
import com.stars.modules.foreshow.ForeShowManager;
import com.stars.modules.foreshow.prodata.ForeShowVo;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/4/11.
 */
public class UnlimitGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        openAll(); // 系统开放
        unlockShopLimit(); // 商店限制
        unlockDungeonLimit(); // 关卡限制
        unlockNewOfflinePvpLimit(); // 演武场限制
        unlockTrialLimit(); // 试炼限制
    }

    private void openAll() {
        for (ForeShowVo vo : ForeShowManager.foreShowVoMap.values()) {
            vo.setOpenlimit("1+1");
        }
        ForeShowManager.foreShowVoMap.get("DailyWindow_1").setOpenlimit("1+999");
        ForeShowManager.foreShowVoMap.get("DailyWindow_2").setOpenlimit("1+999");
        ForeShowManager.foreShowVoMap.get("DailyWindow_18").setOpenlimit("1+999");
        ForeShowManager.foreShowVoMap.get("DailyWindow_23").setOpenlimit("1+999");
        ForeShowManager.foreShowVoMap.get("DailyWindow_24").setOpenlimit("1+999");
        ForeShowManager.foreShowVoMap.get("DailyWindow_30").setOpenlimit("1+999");
        ForeShowManager.foreShowVoMap.get("DailyWindow_8").setOpenlimit("1+999");
        ForeShowManager.foreShowVoMap.get("TrumpWindow").setOpenlimit("1+999");
    }

    private void unlockShopLimit() {
    }

    private void unlockDungeonLimit() {
        for (DungeoninfoVo vo : DungeonManager.dungeonVoMap.values()) {
            vo.setEnterCount(Integer.MAX_VALUE / 2); //
            vo.setReqPower(0);
        }
    }

    private void unlockNewOfflinePvpLimit() {
    }

    private void unlockTrialLimit() {
        // 六国寻宝
        DataManager.commonConfigMap.put(
                "searchtreasure_searchtimes", Integer.toString(Integer.MAX_VALUE / 2));
        // 日常活动
        for (DailyVo vo : DailyManager.getDailyVoMap().values()) {
            if (vo.getCount() > 0) {
                vo.setCount((byte) (Byte.MAX_VALUE / 2));
            }
        }
    }

}
