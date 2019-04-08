package com.stars.cronjob;

import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.util.LogUtil;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by wuyuxing on 2017/3/29.
 */
@DisallowConcurrentExecution
public class FiveOClockResetJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            LoginModuleHelper.FiveOClockResetDaily(false);//每日凌晨五点重置
        } catch (Throwable t) {
            LogUtil.info("每日凌晨五点重置JOB异常", t);
        }
    }
}
