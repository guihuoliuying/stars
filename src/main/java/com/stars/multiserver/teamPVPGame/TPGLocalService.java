package com.stars.multiserver.teamPVPGame;

import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.services.Service;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.Collection;
import java.util.List;

public interface TPGLocalService extends Service, ActorService {
    /**
     * 当前阶段时间是否开启
     *
     * @return
     */
    public boolean isStepTimeActive();

    /**
     * 是否在某个阶段
     *
     * @param stepType
     * @return
     */
    public boolean isInStep(String stepType);

    /**
     * 能否报名
     *
     * @param inititor
     * @return
     */
    public boolean canSignUp(long inititor);

    /**
     * 报名
     *
     * @param teamId
     */
    @AsyncInvocation
    public void signUp(int teamId);

    /**
     * 下发当前活动数据
     *
     * @param initiator
     */
    @AsyncInvocation
    public void reqCurTPGData(long initiator);

    /**
     * 积分赛匹配结果
     *
     * @param initiator
     */
    @AsyncInvocation
    public void reqScoreMatchResult(long initiator);

    /**
     * 更新队员数据
     *
     * @param teamMember
     */
    @AsyncInvocation
    public void updateTPGTeamMember(BaseTeamMember teamMember);

    /**
     * 进入战斗场景(不一定开始战斗)
     *
     * @param initiator
     */
    @AsyncInvocation
    public void enterFight(long initiator);

    /**
     * 处理战斗服帧返回
     *
     * @param fightSceneId
     * @param luaFrameData
     */
    @AsyncInvocation
    public void doFightLuaFram(int serverId, String fightSceneId, LuaFrameData luaFrameData);

    /**
     * 请求积分赛排行榜
     *
     * @param initiator
     */
    @AsyncInvocation
    public void reqScoreRank(long initiator);

    /**
     * 根据队伍id获得排名
     *
     * @param initiator
     * @param teamIdList
     */
    @AsyncInvocation
    public void reqScoreRanking(long initiator, List<Integer> teamIdList);

    /**
     * 通知阶段任务执行(内部使用)
     */
    @AsyncInvocation
    public void maintenance();

    /**
     * 保存
     */
    @AsyncInvocation
    public void save();

    /**
     * 发奖
     *
     * @param teams     需要发奖的队伍(有序)
     * @param awardType
     * @param hasParam
     */
    @AsyncInvocation
    public void grantReward(Collection<TPGTeam> teams, int awardType, boolean hasParam);
}
