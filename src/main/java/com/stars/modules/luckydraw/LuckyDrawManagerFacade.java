package com.stars.modules.luckydraw;

import com.stars.modules.luckydraw.prodata.LuckyPumpAwardVo;
import com.stars.modules.luckydraw1.LuckyDraw1Manager;
import com.stars.modules.luckydraw2.LuckyDraw2Manager;
import com.stars.modules.luckydraw3.LuckyDraw3Manager;
import com.stars.modules.luckydraw4.LuckyDraw4Manager;
import com.stars.modules.operateactivity.OperateActivityConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyDrawManagerFacade {

    public static Map<Integer, LuckyPumpAwardVo> getLuckyPumpAwardMap(int actType) {
        switch (actType) {
            case OperateActivityConstant.ActType_LuckyDraw: {
                return LuckyDrawManager.luckyPumpAwardMap;
            }
            case OperateActivityConstant.ActType_LuckyDraw1: {
                return LuckyDraw1Manager.luckyPumpAwardMap;
            }
            case OperateActivityConstant.ActType_LuckyDraw2: {
                return LuckyDraw2Manager.luckyPumpAwardMap;
            }
            case OperateActivityConstant.ActType_LuckyDraw3: {
                return LuckyDraw3Manager.luckyPumpAwardMap;
            }
            case OperateActivityConstant.ActType_LuckyDraw4: {
                return LuckyDraw4Manager.luckyPumpAwardMap;
            }
        }
        return new HashMap<>();
    }


    public static Map<Integer, Map<Integer, Integer>> getMoneyDrawReward(int actType) {
        switch (actType) {
            case OperateActivityConstant.ActType_LuckyDraw: {
                return LuckyDrawManager.moneyDrawReward;
            }
            case OperateActivityConstant.ActType_LuckyDraw1: {
                return LuckyDraw1Manager.moneyDrawReward;
            }
            case OperateActivityConstant.ActType_LuckyDraw2: {
                return LuckyDraw2Manager.moneyDrawReward;
            }
            case OperateActivityConstant.ActType_LuckyDraw3: {
                return LuckyDraw3Manager.moneyDrawReward;
            }
            case OperateActivityConstant.ActType_LuckyDraw4: {
                return LuckyDraw4Manager.moneyDrawReward;
            }
        }
        return new HashMap<>();
    }


    public static Map<Integer, Integer> getLuckyDrawSwitch(int actType) {
        switch (actType) {
            case OperateActivityConstant.ActType_LuckyDraw: {
                return LuckyDrawManager.luckyDrawSwitch;
            }
            case OperateActivityConstant.ActType_LuckyDraw1: {
                return LuckyDraw1Manager.luckyDrawSwitch;
            }
            case OperateActivityConstant.ActType_LuckyDraw2: {
                return LuckyDraw2Manager.luckyDrawSwitch;
            }
            case OperateActivityConstant.ActType_LuckyDraw3: {
                return LuckyDraw3Manager.luckyDrawSwitch;
            }
            case OperateActivityConstant.ActType_LuckyDraw4: {
                return LuckyDraw4Manager.luckyDrawSwitch;
            }
        }
        return new HashMap<>();
    }

    public static int getLuckyDrawNumlimit(int actType) {
        switch (actType) {
            case OperateActivityConstant.ActType_LuckyDraw: {
                return LuckyDrawManager.luckyDrawNumlimit;
            }
            case OperateActivityConstant.ActType_LuckyDraw1: {
                return LuckyDraw1Manager.luckyDrawNumlimit;
            }
            case OperateActivityConstant.ActType_LuckyDraw2: {
                return LuckyDraw2Manager.luckyDrawNumlimit;
            }
            case OperateActivityConstant.ActType_LuckyDraw3: {
                return LuckyDraw3Manager.luckyDrawNumlimit;
            }
            case OperateActivityConstant.ActType_LuckyDraw4: {
                return LuckyDraw4Manager.luckyDrawNumlimit;
            }
        }
        return 0;
    }


    public static int getLuckyDrawFreeTimes(int actType) {
        switch (actType) {
            case OperateActivityConstant.ActType_LuckyDraw: {
                return LuckyDrawManager.luckyDrawFreeTimes;
            }
            case OperateActivityConstant.ActType_LuckyDraw1: {
                return LuckyDraw1Manager.luckyDrawFreeTimes;
            }
            case OperateActivityConstant.ActType_LuckyDraw2: {
                return LuckyDraw2Manager.luckyDrawFreeTimes;
            }
            case OperateActivityConstant.ActType_LuckyDraw3: {
                return LuckyDraw3Manager.luckyDrawFreeTimes;
            }
            case OperateActivityConstant.ActType_LuckyDraw4: {
                return LuckyDraw4Manager.luckyDrawFreeTimes;
            }
        }
        return 0;
    }


    public static int getLuckyDrawConsumeUnit(int actType) {
        switch (actType) {
            case OperateActivityConstant.ActType_LuckyDraw: {
                return LuckyDrawManager.luckyDrawConsumeUnit;
            }
            case OperateActivityConstant.ActType_LuckyDraw1: {
                return LuckyDraw1Manager.luckyDrawConsumeUnit;
            }
            case OperateActivityConstant.ActType_LuckyDraw2: {
                return LuckyDraw2Manager.luckyDrawConsumeUnit;
            }
            case OperateActivityConstant.ActType_LuckyDraw3: {
                return LuckyDraw3Manager.luckyDrawConsumeUnit;
            }
            case OperateActivityConstant.ActType_LuckyDraw4: {
                return LuckyDraw4Manager.luckyDrawConsumeUnit;
            }
        }
        return 0;
    }


    public static List<LuckyPumpAwardVo> getLuckyPumpAwardList(int actType) {
        switch (actType) {
            case OperateActivityConstant.ActType_LuckyDraw: {
                return LuckyDrawManager.luckyPumpAwardList;
            }
            case OperateActivityConstant.ActType_LuckyDraw1: {
                return LuckyDraw1Manager.luckyPumpAwardList;
            }
            case OperateActivityConstant.ActType_LuckyDraw2: {
                return LuckyDraw2Manager.luckyPumpAwardList;
            }
            case OperateActivityConstant.ActType_LuckyDraw3: {
                return LuckyDraw3Manager.luckyPumpAwardList;
            }
            case OperateActivityConstant.ActType_LuckyDraw4: {
                return LuckyDraw4Manager.luckyPumpAwardList;
            }
        }
        return new ArrayList<>();
    }


    public static String getLuckyPumpMoney(int actType) {
        switch (actType) {
            case OperateActivityConstant.ActType_LuckyDraw: {
                return LuckyDrawManager.luckyPumpMoney;
            }
            case OperateActivityConstant.ActType_LuckyDraw1: {
                return LuckyDraw1Manager.luckyPumpMoney;
            }
            case OperateActivityConstant.ActType_LuckyDraw2: {
                return LuckyDraw2Manager.luckyPumpMoney;
            }
            case OperateActivityConstant.ActType_LuckyDraw3: {
                return LuckyDraw3Manager.luckyPumpMoney;
            }
            case OperateActivityConstant.ActType_LuckyDraw4: {
                return LuckyDraw4Manager.luckyPumpMoney;
            }
        }
        return null;
    }

}
