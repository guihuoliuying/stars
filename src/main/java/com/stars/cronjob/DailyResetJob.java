package com.stars.cronjob;

import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.services.ServiceHelper;
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
            ServiceHelper.familyMainService().resetDaily();
            //好友每日重置
            ServiceHelper.friendService().dailyReset();
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
