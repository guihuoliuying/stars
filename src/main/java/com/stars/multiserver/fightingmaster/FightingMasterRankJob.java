package com.stars.multiserver.fightingmaster;

import com.stars.multiserver.MultiServerHelper;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Calendar;

/**
 * Created by zhouyaohui on 2016/11/22.
 */
public class FightingMasterRankJob implements Job{
    private static Logger logger = LogManager.getLogger("fighting master job");

    @Override
    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        if (MultiServerHelper.isFightingMasterServer() == false) {
            // 不是斗神殿忽视job
            return;
        }
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.WEDNESDAY) {
            }
        } catch (Throwable t) {
            logger.error("斗神殿发奖JOB异常", t);
        }

    }
}
