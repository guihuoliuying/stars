package com.stars.services.family.activities.invade;

import com.stars.core.player.PlayerPacket;
import com.stars.services.Service;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/10/17.
 */
public interface FamilyActInvadeService extends Service, ActorService {
    /**
     * 活动开始
     */
    @AsyncInvocation
    public void start();

    /**
     * 活动时间结束
     */
    @AsyncInvocation
    public void timeOver();

    /**
     * 活动结束
     */
    @AsyncInvocation
    public void end();

    /**
     * 更新玩家数据(上线/进入家族领地)
     *
     * @param familyId
     * @param member
     */
    @AsyncInvocation
    public void addUpdateMember(long familyId, BaseTeamMember member);

    /**
     * 移除玩家数据(退出家族领地)
     *
     * @param familyId
     * @param roleId
     */
    @AsyncInvocation
    public void removeMember(long familyId, long roleId);

    /**
     * 玩家是否在活动中(在家族领地)
     *
     * @param familyId
     * @param roleId
     * @return
     */
    public boolean isMemberIn(long familyId, long roleId);

    /**
     * 战斗结束
     * 更新npc状态,更新伤害排行
     *
     * @param familyId
     * @param monsterNpcUId
     * @param result
     * @param damageMap
     * @param teamId
     */
    @AsyncInvocation
    public void challengeFinish(long familyId, int monsterNpcUId, byte result, Map<String, Integer> damageMap, int teamId);

    /**
     * 请求怪物npc列表
     *
     * @param familyId
     * @param roleId
     */
    @AsyncInvocation
    public void reqMonsterNpc(long familyId, long roleId);

    /**
     * 触发战斗
     *
     * @param familyId
     * @param roleId
     * @param monsterNpcId
     * @param curPosX
     * @param curPosZ
     */
    @AsyncInvocation
    public void triggerFight(long familyId, long roleId, int monsterNpcId, float curPosX, float curPosZ);

    /**
     * 请求伤害排行榜
     *
     * @param familyId
     * @param roleId
     */
    @AsyncInvocation
    public void reqRankList(long familyId, long roleId);

    /**
     * 战斗交互包处理
     *
     * @param familyId
     * @param roleId
     * @param packet
     */
    @AsyncInvocation
    public void receiveFightPacket(long familyId,long roleId, PlayerPacket packet);

    /**
     * 从战斗中退出
     *
     * @param familyId
     * @param roleId
     */
    @AsyncInvocation
    public void quitFromFight(long familyId, long roleId);
}
