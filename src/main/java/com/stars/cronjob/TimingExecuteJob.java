package com.stars.cronjob;

import com.stars.core.player.PlayerSystem;
import com.stars.modules.demologin.message.TimingExecuteMessage;
import com.stars.util.LogUtil;
import com.stars.core.actor.AbstractActor;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/4/27.
 */
@DisallowConcurrentExecution
public class TimingExecuteJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (PlayerSystem.system() == null || PlayerSystem.system().getActors() == null){
            return;
        }
        
        // 遍历Players
        for (Map.Entry<String, AbstractActor> en : PlayerSystem.system().getActors().entrySet()) {
            try {
                en.getValue().tell(new TimingExecuteMessage(), en.getValue());
            } catch (Throwable cause) {
                if (cause instanceof IllegalStateException && cause.getMessage().contains("Queue is full")) {
                    // swallow it
                } else {
                    LogUtil.error("", cause);
                }
            }
        }
    }
}
