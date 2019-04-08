package com.stars.services.elitedungeon;

import com.stars.core.player.PlayerPacket;
import com.stars.modules.elitedungeon.userdata.ElitePlayerImagePo;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;
import com.stars.core.actor.invocation.annotation.DispatchAll;

/**
 * Created by gaopeidian on 2017/3/9.
 */
public interface EliteDungeonService extends Service, ActorService {
	
	@DispatchAll
    @AsyncInvocation
    void save();
	
    /**
     * 新增/更新参与的玩家数据
     *
     * @param roleId
     * @param teamDungeonId
     */
    public boolean addMemberId(long roleId, int eliteDungeonId);

    /**
     * 移除参与的玩家数据
     *
     * @param roleId
     */
    //public boolean removeMemberId(long roleId, int eliteDungeonId);
    @AsyncInvocation
    public void removeMemberId(long roleId, int eliteDungeonId);
    
    /**
     * 移除参与的玩家数据
     *
     * @param roleId
     */
    @AsyncInvocation
    public void removeMemberId(long roleId);

    /**
     * 玩家数据是否可参与
     *
     * @param roleId
     * @param teamDungeonId
     * @return
     */
    public boolean isMemberIn(long roleId, int eliteDungeonId);
    
    /**
     * 添加精英副本玩家镜像数据
     * @param po
     */
    public void addPlayerImageData(ElitePlayerImagePo po);

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
    
    /**
     * 副本开始战斗
     *
     * @param roleId
     */
    @AsyncInvocation
    public void startFightTime(long roleId);
    

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
    
    /**
     * 执行定时任务
     */
    @AsyncInvocation
    public void executeTask();
}
