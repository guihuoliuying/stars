package com.stars.services.newserverfightscore;

import com.stars.modules.newserverfightscore.userdata.ActRoleNsFightScore;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by liuyuheng on 2017/1/5.
 */
public interface NewServerFightScoreService extends Service, ActorService {
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
     * 每日重置
     */
    @AsyncInvocation
    public void dailyReset();

    /**
     * 更新领奖记录
     *
     * @param roleId
     * @param po
     */
    @AsyncInvocation
    public void updateRewardRecord(long roleId, ActRoleNsFightScore po);

    /**
     * 下发历史排行榜
     *
     * @param roleId
     */
    @AsyncInvocation
    public void sendHistoryRank(long roleId);

    /**
     * 战力排名是否达到
     *
     * @param roleId
     * @param rankingLimit
     * @return 返回排名值,-1=未达到
     */
    public int canRankingReward(long roleId, int[] rankingLimit);
}
