package com.stars.services.teamdungeon;

import com.stars.core.player.PlayerPacket;
import com.stars.services.Service;
import com.stars.services.baseteam.BaseTeam;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by liuyuheng on 2016/11/14.
 */
public interface TeamDungeonService extends Service, ActorService {
    /**
     * 新增/更新参与的玩家数据
     *
     * @param roleId
     * @param teamDungeonId
     */
    public boolean addMemberId(long roleId, int teamDungeonId);

    /**
     * 移除参与的玩家数据
     *
     * @param roleId
     */
    public boolean removeMemberId(long roleId, int teamDungeonId);

    /**
     * 玩家数据是否可参与
     *
     * @param roleId
     * @param teamDungeonId
     * @return
     */
    public boolean isMemberIn(long roleId, int teamDungeonId);

    /**
     * 下发可邀请列表
     *
     * @param initiator
     * @param target
     * @param scene
     */
    @AsyncInvocation
    public void sendCanInviteList(long initiator, int target, String scene);

    /**
     * 进入副本战斗
     *
     * @param initiator
     */
    @AsyncInvocation
    public void enterFight(long initiator);

    @AsyncInvocation
    void enterMarryFight(long initiator);

    /**
     * 组队副本回城
     *
     * @param initiator
     */
    @AsyncInvocation
    public void backToCity(long initiator);

    /**
     * 在副本中死了，且没复活次数了，自动回城
     *
     * @param initiator
     */
    @AsyncInvocation
    public void deadInDungeon(long initiator);

    /**
     * 副本中收到战斗相关包
     *
     * @param packet
     */
    @AsyncInvocation
    public void receiveFightPacket(PlayerPacket packet);

    @AsyncInvocation
    void receiveMarryFightPacket(PlayerPacket packet);

    /**
     * 复活检查
     *
     * @param roleId
     * @return
     */
    public boolean checkResurgence(long roleId);

    /**
     * 销毁fighscene
     *
     * @param teamId
     */
    @AsyncInvocation
    public void removeFightScene(int teamId);

    @AsyncInvocation
    void removeFightScene(long roleId, BaseTeam team);

    /**
     * 执行定时任务
     */
    @AsyncInvocation
    public void executeTask();
}
