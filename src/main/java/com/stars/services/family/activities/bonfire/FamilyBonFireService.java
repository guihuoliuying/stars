package com.stars.services.family.activities.bonfire;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by wuyuxing on 2017/3/9.
 */
public interface FamilyBonFireService extends Service, ActorService {
    /**
     * 活动开始
     */
    @AsyncInvocation
    public void start();

    /**
     * 活动结束
     */
    @AsyncInvocation
    public void end();

    /**
     * 更新玩家数据(上线/进入家族篝火场景)
     *
     * @param familyId
     */
    @AsyncInvocation
    public void addUpdateMember(long familyId, long roleId,int roleLevel,int roleJob);

    @AsyncInvocation
    public void updateRoleLevel(long familyId,long roleId,int roleLevel);

    /**
     * 移除玩家数据(退出家族篝火场景)
     *
     * @param familyId
     * @param roleId
     */
    @AsyncInvocation
    public void removeMember(long familyId, long roleId);

    @AsyncInvocation
    public void updateRoleFire(long familyId, long roleId);

    @AsyncInvocation
    public void initRoleFireInfo(long familyId, long roleId,int roleLevel,int roleJob,int remainThrowGoldTimes);

    /**
     * 增加篝火经验
     */
    @AsyncInvocation
    public void addFamilyFireExp(long familyId, long roleId,int exp);

    /**
     * 捡干柴并立刻投掷
     */
    @AsyncInvocation
    public void pickWood(long familyId, long roleId);

    /**
     * 刷新题目,并下发给所有在篝火场景的玩家
     */
    @AsyncInvocation
    public void sendCurQuestionToOnline();

    /**
     * 每日重置
     */
    @AsyncInvocation
    public void dailyReset();

    /**
     * 篝火答题
     */
    @AsyncInvocation
    public void answerQuestion(long familyId, long roleId,String name,int questionId,int questionIndex);

    /**
     * 答题结束
     */
    @AsyncInvocation
    public void questionEnd();

}
