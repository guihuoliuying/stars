package com.stars.services.escort;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerPacket;
import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by wuyuxing on 2016/12/5.
 */
public interface EscortService extends Service, ActorService {

    /**
     * 开始个人押镖
     */
    @AsyncInvocation
    public void singleBeginEscort(long roleId, Map<String, Module> moduleMap, byte index, int carId, long familyId);

    /**
     * 开始组队押镖
     */
    @AsyncInvocation
    public void teamBeginEscort(long leaderId, byte index,int carId,long familyId);

    /**
     * 单人进入队列场景
     */
    @AsyncInvocation
    public void singleEnterCargoListScene(long roleId,List<Long> enemyList,int fighting);

    /**
     * 组队进入队列场景
     */
    @AsyncInvocation
    public void teamEnterCargoListScene(long roleId,List<Long> enemyList);

    @AsyncInvocation
    public void onFightCreationSucceeded(int mainServerId, int fightServerId, String fightId,List<Long> escortIds);

    @AsyncInvocation
    public void onFighterAddingSucceeded(int mainServerId, int fightServerId, String fightId, Set<Long> entitySet);

    /**
     * 刷新镖车列表场景
     */
    @AsyncInvocation
    public void updateCargoListScene(long roleId,byte refreshIndex);

    /**
     * 进入运镖场景进行劫镖
     */
    @AsyncInvocation
    public void robCargo(long roleId, byte index,byte escortType,Map<String, Module> moduleMap,boolean useMask,int remainRobTimes);

    /**
     * 进入机器人镖车关卡(劫镖)
     */
    @AsyncInvocation
    public void robRobotCargo(long roleId, int sectionId,byte escortType,Map<String, Module> moduleMap);

    /**
     * 战斗数据处理
     */
    @AsyncInvocation
    public void doFightFramData(int serverId, String fightId, LuaFrameData lData);

    /**
     * 劫镖超时处理
     */
    @AsyncInvocation
    public void handleTimeOut(int fromServerId, String fightId, HashMap<String, String> hpInfo);

    /**
     * 死亡处理
     */
    @AsyncInvocation
    public void handleDead(int serverId, String fightId, Map<String, String> deadMap);

    /**
     * 继续运镖
     */
    @AsyncInvocation
    public void escortContinue(long roleId);

    /**
     * 副本中收到战斗相关包
     */
    @AsyncInvocation
    public void receiveFightPacket(PlayerPacket packet);

    @AsyncInvocation
    public void handleOffline(int fromServerId, String fightId, long roleId);

    @AsyncInvocation
    public void handleOffline(long roleId);

    /**
     * 镖车队列场景的掉线处理
     */
    @AsyncInvocation
    public void handleOfflineInSafeScene(long roleId);

    /**
     * 劫镖后回城处理
     */
    @AsyncInvocation
    public void leaveSceneAfterRob(long roleId,byte success);

    /**
     * 加入运镖组队权限集合
     */
    public void addToEscortPermitSet(long roleId);

    /**
     * 从运镖组队权限集合中移除
     */
    public void removeFromEscortPermitSet(long roleId);

    /**
     * 加入劫镖组队权限集合
     */
    public void addToRobPermitSet(long roleId);

    /**
     * 从劫镖组队权限集合中移除
     */
    public void removeFromRobPermitSet(long roleId);

    /**
     * 是否有运镖组队权限(队长同意组队申请时判定)
     */
    public boolean hasJoinEscortTeamPermit(long roleId);

    /**
     * 是否有劫镖组队权限(队长同意组队申请时判定)
     */
    public boolean hasJoinRobTeamPermit(long roleId);

}
