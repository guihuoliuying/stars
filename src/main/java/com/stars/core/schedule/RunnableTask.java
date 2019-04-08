package com.stars.core.schedule;

import com.stars.util.LogUtil;

/**
 * Created by wuyuxing on 2017/1/20.
 */
public class RunnableTask implements Runnable {

    private Runnable task;

    public RunnableTask(Runnable task) {
        this.task = task;
    }

    @Override
    public void run() {
        try {
            if(task!=null){
                task.run();
            }
        }catch (Exception e){
            LogUtil.error(e.getMessage(),e);
        }
    }
}
