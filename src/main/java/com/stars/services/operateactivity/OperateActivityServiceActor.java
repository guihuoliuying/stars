package com.stars.services.operateactivity;

import com.stars.modules.data.DataManager;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.operateactivity.opentime.*;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.multiserver.MultiServerHelper;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by gaopeidian on 2016/12/26.
 */
public class OperateActivityServiceActor extends ServiceActor implements OperateActivityService {
    //<活动类型,活动id>,活动id为-1时代表此类型无正在进行中的活动
    private ConcurrentHashMap<Integer, Integer> curActivityIds = new ConcurrentHashMap<Integer, Integer>();

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.OperateActivityService, this);

        checkOpActivities(true);
    }

    @Override
    public void printState() {
        LogUtil.info("容器大小输出:{},curActivityIds:{}", this.getClass().getSimpleName(), curActivityIds.size());
    }

    //    @Override
//    public void save() {
//        dao.flush();
//    }

//    public void dailyReset() {
//        
//    }

    /**
     * @param isInit：是否是起服是检查
     */
    @Override
    public void checkOpActivities(boolean isInit) {
        Date nowDate = new Date();
        int openServerDay = DataManager.getServerDays();
        Map<Integer, String> opCheckMap = OperateActivityManager.getOpCheckMap();
        Set<Map.Entry<Integer, String>> entrySet = opCheckMap.entrySet();
        for (Map.Entry<Integer, String> entry : entrySet) {
            int opType = entry.getKey();
            checkOpActivityByType(opType, nowDate, openServerDay, isInit);
        }
    }

    /**
     */
    private void checkOpActivityByType(int type, Date nowDate, int openServerDay, boolean isInit) {
        int curActivityId = -1;
        if (curActivityIds.containsKey(type)) {
            curActivityId = curActivityIds.get(type);
        }

        if (curActivityId != -1) {//若当前有正在进行的活动，则检查是否需要关掉
            OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
            if (operateActVo != null
                    && isOn(operateActVo.getOpen(), operateActVo.getActOpenTimeBase(), nowDate, openServerDay)
                    && isServerOpen(operateActVo.getBeginServer(), operateActVo.getEndServer())) {
                return;
            } else {
                curActivityIds.put(type, -1);//关闭活动
                OperateActivityManager.setCurActivityIds(new ConcurrentHashMap<Integer, Integer>(curActivityIds));//更新活动状态
                if (!isInit) {
                    //tellClose
                    tellCloseActivity(type, curActivityId);
                }
                curActivityId = -1;
            }
        }

        if (curActivityId == -1) {//若当前无正在进行的活动，则检查是否需要开启一个新的
            List<OperateActVo> operateActVos = OperateActivityManager.getOperateActVoListByType(type);
            if (operateActVos != null) {
                for (OperateActVo operateActVo : operateActVos) {
                    if (isOn(operateActVo.getOpen(), operateActVo.getActOpenTimeBase(), nowDate, openServerDay)
                            && isServerOpen(operateActVo.getBeginServer(), operateActVo.getEndServer())) {
                        int activityId = operateActVo.getOperateactid();
                        curActivityIds.put(type, activityId);//开启活动
                        OperateActivityManager.setCurActivityIds(new ConcurrentHashMap<Integer, Integer>(curActivityIds));//更新活动状态

                        if (!isInit) {
                            //tellOpen
                            tellOpenActivity(type, activityId);
                        }

                        break;//已有开启的活动了，不再检查其他的，跳出循环
                    }
                }
            }
        }
    }

    private void tellOpenActivity(int opType, int activityId) {
        ServiceHelper.roleService().noticeAll(new OperateActivityEvent(OperateActivityEvent.Flag_Open_Activity, opType, activityId));

        if (opType == OperateActivityConstant.ActType_NewServerRank) {
            ServiceHelper.newServerRankService().openActivity(activityId);
        } else if (opType == OperateActivityConstant.ActType_NewServerMoney) {
            ServiceHelper.newServerMoneyService().openActivity(activityId);
        } else if (opType == OperateActivityConstant.ActType_NewServerFightScore) {
            ServiceHelper.newServerFightScoreService().openActivity(activityId);
        } else if (opType == OperateActivityConstant.ActType_BestCP520) {
            ServiceHelper.opBestCPService().openActivity(activityId);
        } else if (opType == OperateActivityConstant.ActType_DragBoat) {
            ServiceHelper.opDragonBoatService().openActivity(activityId);
        } else if (opType == OperateActivityConstant.ActType_WeeklyGift) {
            ServiceHelper.weeklyGiftService().initActivity();
        } else if (opType == OperateActivityConstant.ActType_LuckyTurnTable) {
            ServiceHelper.luckyTurnTableService().initActivity();
        } else if (opType == OperateActivityConstant.ActType_MoonCake) {
            ServiceHelper.moonCakeService().initMoonCake();
        }
    }

    private void tellCloseActivity(int opType, int activityId) {
        ServiceHelper.roleService().noticeAll(new OperateActivityEvent(OperateActivityEvent.Flag_Close_Activity, opType, activityId));

        if (opType == OperateActivityConstant.ActType_NewServerRank) {
            ServiceHelper.newServerRankService().closeActivity(activityId);
        } else if (opType == OperateActivityConstant.ActType_NewServerMoney) {
            ServiceHelper.newServerMoneyService().closeActivity(activityId);
        } else if (opType == OperateActivityConstant.ActType_NewServerFightScore) {
            ServiceHelper.newServerFightScoreService().closeActivity(activityId);
        } else if (opType == OperateActivityConstant.ActType_ChargeScore) {
            ServiceHelper.opActChargeScore().closeActivity(activityId);
        } else if (opType == OperateActivityConstant.ActType_BestCP520) {
            ServiceHelper.opBestCPService().closeActivity(activityId);
        } else if (opType == OperateActivityConstant.ActType_DragBoat) {
            ServiceHelper.opDragonBoatService().closeActivity(activityId);
        } else if (opType == OperateActivityConstant.ActType_WeeklyCharge) {
            ServiceHelper.weeklyChargeService().closeActivity(activityId);
        } else if (opType == OperateActivityConstant.ActType_WeeklyGift) {
            ServiceHelper.weeklyGiftService().closeActivity();
        } else if (opType == OperateActivityConstant.ActType_LuckyTurnTable) {
            ServiceHelper.luckyTurnTableService().closeActivity();
        } else if (opType == OperateActivityConstant.ActType_MoonCake) {
            ServiceHelper.moonCakeService().closeMoonCake();
        }
    }

    public static boolean isOn(byte isOpen, ActOpenTimeBase openTimeBase, Date nowDate, int openServerDay) {
        if (isOpen == (byte) 0) return false;

        if (openTimeBase == null || nowDate == null) return false;

        if (openTimeBase instanceof ActOpenTime0) {//永久开放
            return true;
        } else if (openTimeBase instanceof ActOpenTime1) {//固定时间开放
            ActOpenTime1 openTime1 = (ActOpenTime1) openTimeBase;
            Date startDate = openTime1.getStartDate();
            Date endDate = openTime1.getEndDate();
            if (!DateUtil.isBetween(nowDate, startDate, endDate)) return false;
            int startDay = DataManager.getServerDays(startDate.getTime());
            return startDay > 0;
        } else if (openTimeBase instanceof ActOpenTime2) {//固定时间+开服前几天内开放
            ActOpenTime2 openTime2 = (ActOpenTime2) openTimeBase;
            Date startDate = openTime2.getStartDate();
            Date endDate = openTime2.getEndDate();
            if (!DateUtil.isBetween(nowDate, startDate, endDate)) return false;
            int limitDay = openTime2.getServerLimitDay();
            if (limitDay == -1) return false;
            int startDay = DataManager.getServerDays(startDate.getTime());
            return startDay > limitDay;
        } else if (openTimeBase instanceof ActOpenTime3) {//开服x~y天内开启
            ActOpenTime3 openTime3 = (ActOpenTime3) openTimeBase;
            int startDays = openTime3.getStartDays();
            int endDays = openTime3.getEndDays();
            if (startDays == -1 || endDays == -1) return false;

            return openServerDay >= startDays && openServerDay <= endDays;
        } else if (openTimeBase instanceof ActOpenTime4) {//创角x~y天内开启，与玩家相关，所以是永久
            return true;
        } else if (openTimeBase instanceof ActOpenTime5) {//固定时间开放
            ActOpenTime5 actOpenTime5 = (ActOpenTime5) openTimeBase;
            Date startDate = actOpenTime5.getStartDate();
            Date endDate = actOpenTime5.getEndDate();
            return DateUtil.isBetween(nowDate, startDate, endDate);
        } else if (openTimeBase instanceof ActOpenTime6) {//开服x天内不开此活动,之后开启
            ActOpenTime6 openTime6 = (ActOpenTime6) openTimeBase;
            int days = openTime6.getDays();
            if (days == -1) return false;
            return openServerDay > days;
        }

        return false;
    }

    public static boolean isServerOpen(int beginServerId, int endServerId) {
        if (beginServerId == -1 || endServerId == -1) {//若有一个为-1，则表示无限制，返回true
            return true;
        }

        int serverId = MultiServerHelper.getServerId();
        return serverId >= beginServerId && serverId <= endServerId;
    }
}
