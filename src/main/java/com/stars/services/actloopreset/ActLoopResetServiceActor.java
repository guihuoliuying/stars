package com.stars.services.actloopreset;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.activityflow.ActivityFlowUtil;
import com.stars.core.module.ModuleManager;
import com.stars.core.db.DBUtil;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.opentime.ActOpenTime2;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.actloopreset.event.ActLoopResetEvent;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by huwenjun on 2017/11/28.
 */
public class ActLoopResetServiceActor extends ServiceActor implements ActLoopResetService {
    public static final String dateFmt = "yyyy-MM-dd HH:mm:ss";

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.ActLoopResetService, this);
        ActLoopResetFlow actLoopResetFlow = new ActLoopResetFlow();
        Map<Integer, String> flowMap = new HashMap<>();
        flowMap.put(1, "0 0 5 ? * 4");
        actLoopResetFlow.init(SchedulerHelper.getScheduler(), flowMap);

    }

    @Override
    public void printState() {

    }

    @Override
    public void resetAndLoop() {
        LogUtil.info("actLoopReset start");
        List<Integer> opTypes = new ArrayList<>();
        opTypes.add(4006);
        opTypes.add(4007);
        opTypes.add(3009);
        opTypes.add(4016);
        opTypes.add(3002);
        opTypes.add(4018);
        List<String> sqls = new ArrayList<>();
        for (Integer type : opTypes) {
            StringBuilder openTime = new StringBuilder();
            OperateActVo operateActVo = OperateActivityManager.getOperateActVoListByType(type).get(0);

            long beginDay = 0;
            long endDay = 0;
            if (type == 4006) {
                /**
                 * 周末充值送礼,仅两天
                 */
                openTime.append("5|");
                String beginCron = "";
                beginCron = "0 0 0 ? * 7";
                beginDay = ActivityFlowUtil.getTimeInMillisByCronExpr(beginCron);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(beginDay);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                endDay = calendar.getTimeInMillis();
            } else {
                if (operateActVo.getActOpenTimeBase() instanceof ActOpenTime2) {
                    openTime.append("2|");
                } else {
                    openTime.append("5|");
                }
                String cron = "0 0 6 ? * 4";
                beginDay = ActivityFlowUtil.getTimeInMillisByCronExpr(cron);
                Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTimeInMillis(beginDay);
                endCalendar.add(Calendar.WEEK_OF_MONTH, 1);
                endCalendar.add(Calendar.DAY_OF_YEAR, -1);
                endCalendar.set(Calendar.HOUR_OF_DAY, 23);
                endCalendar.set(Calendar.MINUTE, 59);
                endCalendar.set(Calendar.SECOND, 59);
                endDay = endCalendar.getTimeInMillis();
            }

            String beginDate = DateUtil.formatDate(new Date(beginDay), dateFmt);
            String endDate = DateUtil.formatDate(new Date(endDay), dateFmt);
            openTime.append(beginDate).append("&").append(endDate);
            if (operateActVo.getActOpenTimeBase() instanceof ActOpenTime2) {
                ActOpenTime2 actOpenTime2 = (ActOpenTime2) operateActVo.getActOpenTimeBase();
                openTime.append(";").append(actOpenTime2.getServerLimitDay());
            }
            String sql = String.format("update operateact set opentime='%s' where type=%s;", openTime.toString(), type);
            LogUtil.info("actLoopReset sql="+sql);
            sqls.add(sql);
        }
        try {
            List<String> userSqls = new ArrayList<>();
            userSqls.add("delete from chargegift;");//4006
            userSqls.add("delete from roleweeklycharge;");//4007
            userSqls.add("delete from rolenewfirstrecharge where activityType=3009;");//3009
            userSqls.add("delete from newroledailycharge;");//4016
            userSqls.add("delete from roleconsumeinfo;");//3002
            userSqls.add("delete from rolemooncake;");//4018
            DBUtil.execBatch(DBUtil.DB_USER, false, userSqls);
            DBUtil.execBatch(DBUtil.DB_PRODUCT, true, sqls);
        } catch (SQLException e) {
            LogUtil.error("修改产品库时间失败", e);
            throw new RuntimeException(e);
        }
        try {
            ModuleManager.loadProductData("operateactivity");
        } catch (Exception e) {
            LogUtil.error("重载活动产品表失败", e);
            throw new RuntimeException(e);
        }
        ServiceHelper.roleService().noticeAll(new ActLoopResetEvent());
        LogUtil.info("actLoopReset success");
    }

    public static void main(String[] args) {
//        long timeInMillisByCronExpr = ActivityFlowUtil.getTimeInMillisByCronExpr("0 0 0 ? * 1");
        long timeInMillisByCronExpr = ActivityFlowUtil.getTimeInMillisByCronExpr("0 0 0 ? * 7");
        Date date = new Date(2017, 10, 30, 0, 0, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
//        calendar.add(Calendar.WEEK_OF_MONTH, 3);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        System.out.println(DateUtil.formatDateTime(calendar.getTimeInMillis()));
        System.out.println(DateUtil.formatDate(new Date(timeInMillisByCronExpr), "yyyy-MM-dd HH:mm:ss"));
    }
}
