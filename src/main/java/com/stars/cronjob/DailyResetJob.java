package com.stars.cronjob;

import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.services.ServiceHelper;
import com.stars.services.rank.RankConstant;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Calendar;

/**
 * Created by zhaowenshuo on 2016/5/11.
 */
@DisallowConcurrentExecution
public class DailyResetJob implements Job {

    private static Logger logger = LogManager.getLogger("job");

    @Override
    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        try {
            LoginModuleHelper.resetDaily(false);
            // 排行榜每日发奖
            ServiceHelper.rankService().rewardHandler(RankConstant.REWARD_TYPE_DAILY);
            // 排行榜每日重置
            ServiceHelper.rankService().dailyReset();
            ServiceHelper.familyMainService().resetDaily();
            // 召唤boss每日重置
            ServiceHelper.callBossService().dailyReset();
            // 离线pvp每日重置
            ServiceHelper.offlinePvpService().dailyReset();
            //好友每日重置
            ServiceHelper.friendService().dailyReset();
            //活动每日重置
            //ServiceHelper.operateActivityService().dailyReset();
            // 新服(撒钱/冲战力)活动每日重置,非业务相关的重置
            ServiceHelper.newServerMoneyService().dailyReset();
            ServiceHelper.newServerFightScoreService().dailyReset();

            //重置篝火活动题目
            ServiceHelper.familyBonFireService().dailyReset();
            //家族任务帮助列表重置
            ServiceHelper.familyTaskService().dailyReset();
            //家族运镖次数重置
//            ServiceHelper.familyEscortService().dailyReset();
            //天梯积分次数重置
            ServiceHelper.skyRankLocalService().dailyReset();
            //等级加速  条件值每日重置
            ServiceHelper.levelSpeedUpService().conditionReset();
            //周慧礼包，每日重置
            ServiceHelper.weeklyGiftService().dailyReset();

            ServiceHelper.familyWarService().dailyCheck();

        } catch (Throwable t) {
            logger.error("每日重置JOB异常", t);
        }
        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        // 周日重置
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.MONDAY) {
            try {
                LoginModuleHelper.resetWeekly(false);
            } catch (Throwable t) {
                logger.error("每周重置JOB异常", t);
            }
            try {
                // 排行榜每周发奖
                ServiceHelper.rankService().rewardHandler(RankConstant.REWARD_TYPE_WEEKLY);
                // todo: fill me
            } catch (Throwable t) {
                logger.error("每周重置JOB异常", t);
            }
        }
        // 月重置
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        if (dayOfMonth == 1) {
            try {
                LoginModuleHelper.resetMonthly(false);
            } catch (Throwable t) {
                logger.error("每月重置JOB异常", t);
            }
        }
    }

}
