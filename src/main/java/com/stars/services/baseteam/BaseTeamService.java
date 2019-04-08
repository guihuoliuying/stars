package com.stars.services.baseteam;

import com.stars.modules.baseteam.userdata.BaseTeamInvitor;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;
import com.stars.core.actor.invocation.annotation.DispatchAll;

import java.util.List;

/**
 * Created by liuyuheng on 2016/11/9.
 */
public interface BaseTeamService extends Service, ActorService {
    /*------------------ 同步接口 */

    /**
     * 获得队伍副本类型
     *
     * @param teamId
     * @return
     */
    public byte getTeamType(int teamId);

    /**
     * 获得当前队伍
     *
     * @param roleId
     * @return
     */
    public BaseTeam getTeam(long roleId);

    /**
     * 是否是队长
     */
    public boolean isCaptain(long roleId);

    /**
     * 获得队伍id，无队伍时返回-1
     */
    public int getTeamId(long roleId);

    /**
     * 获得队伍中玩家总战力
     */
    public int getTeamFighting(long roleId);

    /**
     * 获得队伍
     *
     * @param teamId
     * @return
     */
    public BaseTeam getTeam(int teamId);

    /**
     * 是否在队伍中
     *
     * @param roleId
     * @return
     */
    public boolean hasTeam(long roleId);

    /**
     * 创建队伍(同步)
     *
     * @param creator
     * @param teamType
     * @param minMemberCount
     * @param maxMemberCount
     * @param target
     */
    public BaseTeam createTeamSync(BaseTeamMember creator, byte teamType, byte minMemberCount, byte maxMemberCount, int target);

    /*------------------ 异步接口  */

    /**
     * 更新队伍成员
     *
     * @param teamMember
     */
    @AsyncInvocation
    public void updateTeamMember(BaseTeamMember teamMember);

    /**
     * 创建队伍
     *
     * @param creator
     * @param teamType
     * @param minMemberCount
     * @param maxMemberCount
     * @param target
     */
    @AsyncInvocation
    public void createTeam(BaseTeamMember creator, byte teamType, byte minMemberCount, byte maxMemberCount, int target);

    /**
     * 创建队伍加入假队员数据并下发
     *
     * @param creator
     * @param teamType
     * @param minMemberCount
     * @param maxMemberCount
     * @param target
     * @param fakeMembers
     */
    @AsyncInvocation
    public void createTeamWithFakePlayer(BaseTeamMember creator, byte teamType, byte minMemberCount, byte maxMemberCount,
                                         int target, List<BaseTeamMember> fakeMembers);

    /**
     * 可申请队伍列表
     *
     * @param initiator
     * @param teamType
     */
    @AsyncInvocation
    public void canApplyTeam(long initiator, byte teamType);

    /**
     * 开始匹配队伍
     *
     * @param teamMember
     * @param teamType
     * @param target
     */
    @AsyncInvocation
    public void startMatchTeam(BaseTeamMember teamMember, byte teamType, int target);


    /**
     * 取消匹配队伍
     *
     * @param initiator
     * @param teamType
     * @param target
     */
    @AsyncInvocation
    public void cancelMatchTeam(long initiator, byte teamType, int target);

    /**
     * 匹配队员
     *
     * @param initiator
     */
    @AsyncInvocation
    public void matchMemeber(long initiator);

    /**
     * 匹配队员,加入假数据队员
     *
     * @param initiator
     * @param fakeMembers
     */
    @AsyncInvocation
    public void matchFakeMember(long initiator, List<BaseTeamMember> fakeMembers);

    /**
     * 邀请组队
     *
     * @param invitor
     * @param inviteeId
     */
    @AsyncInvocation
    public void inviteJoinTeam(BaseTeamInvitor invitor, long inviteeId);

    @AsyncInvocation
    @DispatchAll
    void inviteJoinTeam(BaseTeamInvitor tInvitor);


    /**
     * 同意邀请
     *
     * @param teamId
     * @param teamMember
     */
    @AsyncInvocation
    public void permitInvite(int teamId, BaseTeamMember teamMember);

    /**
     * 请求申请列表
     *
     * @param initiator
     */
    @AsyncInvocation
    public void reqApplyList(long initiator);

    /**
     * 申请入队
     *
     * @param teamId
     * @param applier
     */
    @AsyncInvocation
    public void applyJoinTeam(int teamId, BaseTeamMember applier);

    /**
     * 同意申请(队长操作)
     *
     * @param initiator
     * @param target
     */
    @AsyncInvocation
    public void permitApply(long initiator, long target);

    /**
     * 拒绝申请(队长操作)
     *
     * @param initiator
     * @param target
     */
    @AsyncInvocation
    public void refuseApply(long initiator, long target);

    /**
     * 请求清空申请列表
     *
     * @param initiator
     */
    @AsyncInvocation
    public void reqClearApplyList(long initiator);

    /**
     * 加入队伍(从聊天窗口直接加入)
     *
     * @param teamId
     * @param teamMember
     */
    @AsyncInvocation
    public void joinTeam(int teamId, BaseTeamMember teamMember);

    /**
     * 退出队伍
     *
     * @param initiator
     */
    @AsyncInvocation
    public void leaveTeam(long initiator);

    /**
     * 踢出队伍(队长操作)
     *
     * @param initiator
     * @param target
     */
    @AsyncInvocation
    public void kickOutTeam(long initiator, long target);

    /**
     * 转让队长(队长操作)
     *
     * @param initiator
     * @param target
     */
    @AsyncInvocation
    public void changeCaptain(long initiator, long target);

    /**
     * 改变组队目标(这里只进行组队数据的改变,由业务发起判断成功后才到这里)
     *
     * @param initiator
     * @param target
     */
    @AsyncInvocation
    public void changeTeamTarget(long initiator, int target);

    /**
     * 设置开放申请标志
     *
     * @param initiator
     * @param flag
     */
    @AsyncInvocation
    public void setAutoApplyFlag(long initiator, byte flag);

    /**
     * 解散某一类型的所有队伍
     *
     * @param teamType 队伍类型
     */
    @AsyncInvocation
    public void disbandTeamByTeamtype(byte teamType);

    /**
     * 解散队伍
     */
    @AsyncInvocation
    public void disbandTeam(int teamId);

    /**
     * 解散队伍
     */
    @AsyncInvocation
    public void disbandTeam(long roleId);

    /**
     * 定时调度
     */
    @AsyncInvocation
    void schedule();



}
