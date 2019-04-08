package com.stars.services.dragonboat;

import com.stars.modules.dragonboat.define.StepTime;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by huwenjun on 2017/5/9.
 */
public interface OpDragonBoatService extends Service, ActorService {
    /**
     * 开始下注
     *
     * @param activityId
     */
    @AsyncInvocation
    void openActivity(Integer activityId);

    /**
     * 开始下注
     *
     * @param activityId
     */
    @AsyncInvocation
    void startBetOn();

    /**
     * 开始竞速
     */
    @AsyncInvocation
    void startRacing();

    /**
     * 进入等待阶段
     */
    @AsyncInvocation
    void startWaiting();

    /**
     * 进入展示阶段
     */
    @AsyncInvocation
    void startShow();

    /**
     * 下注
     *
     * @param roleId
     * @param dragonBoatId
     */
    @AsyncInvocation
    void betOn(long roleId, int dragonBoatId);

    /**
     * 更新龙舟速度
     *
     * @param roleId
     * @param dragonBoatId
     * @param changedSpeed
     */
    @AsyncInvocation
    void updateSpeed(long roleId, int dragonBoatId, int changedSpeed);

    /**
     * 结束一轮
     */
    @AsyncInvocation
    void finishRound();

    /**
     * 获取被下注的龙舟
     *
     * @param roleId
     * @return
     */
    Integer getBetOnDragonBoatId(Long roleId);


    Long getActivityKey();

    /**
     * 每日重置
     */
    @AsyncInvocation
    void closeActivity(int activityId);

    @AsyncInvocation
    void updateSpeedFake();

    @AsyncInvocation
    void updateVoteFake();

    void updateStepTime();

    StepTime nextStep();
}
