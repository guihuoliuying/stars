package com.stars.services.newservermoney;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by liuyuheng on 2017/1/4.
 */
public interface NewServerMoneyService extends Service, ActorService {
    /**
     * 活动开启
     *
     * @param activityId
     */
    @AsyncInvocation
    public void openActivity(int activityId);

    /**
     * 活动结束
     *
     * @param activityId
     */
    @AsyncInvocation
    public void closeActivity(int activityId);

    /**
     * 执行定时任务
     */
    @AsyncInvocation
    public void executeTask();

    /**
     * 每日重置
     */
    @AsyncInvocation
    public void dailyReset();
}
