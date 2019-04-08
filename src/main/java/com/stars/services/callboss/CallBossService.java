package com.stars.services.callboss;

import com.stars.core.player.PlayerPacket;
import com.stars.services.Service;
import com.stars.services.callboss.cache.CallBossCache;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.Set;

/**
 * Created by liuyuheng on 2016/9/5.
 */
public interface CallBossService extends Service, ActorService {
    /**
     * 获得boss状态(同步),会注入玩家造成伤害记录
     *
     * @param roleId
     * @param bossId
     * @return
     */
    CallBossCache getCallBossCache(long roleId, int bossId);

    /**
     * 召唤boss操作(同步)
     * boss状态判断,召唤成功通知客户端更新boss状态
     *
     * @param roleId
     * @param roleName
     * @param bossId
     * @param rewardGroupId
     * @return 召唤失败返回false
     */
    boolean executeCallBoss(long roleId, String roleName, int bossId, byte rewardGroupId);

    /**
     * 更新角色造成伤害
     *
     * @param roldId
     * @param roleName
     * @param bossId
     * @param damage
     */
    @AsyncInvocation
    void addRoleDamage(long roldId, String roleName, int bossId, int damage);

    /**
     * 发送伤害排行榜列表
     *
     * @param roleId
     */
    @AsyncInvocation
    void sendDamageRankList(long roleId);

    /**
     * 根据召唤序列唯一Id发送伤害排行榜
     *
     * @param roleId
     * @param rankUniqueId
     */
    @AsyncInvocation
    void sendDamageRank(long roleId, int rankUniqueId);

    /**
     * 请求下发产品数据,BOSS状态数据
     *
     * @param roleId
     */
    @AsyncInvocation
    void sendCallBossData(long roleId);

    /**
     * 下发单只BOSS的数据（需要同步）
     *
     * @param roleId
     */
    void sendCallBossData(long roleId , int bossId);
    
    /**
     * 每日重置,清空所有排行榜记录,重置召唤序列UIdSeq
     */
    @AsyncInvocation
    void dailyReset();

    /* 内部使用 */
    /**
     * 发奖
     *
     * @param recordUId
     */
    @AsyncInvocation
    void reward(int recordUId);

    /**
     * 推送所有在线玩家,更新boss状态
     *
     * @param packet
     * @param exceptRoleIds 不推送的角色Id
     */
    @AsyncInvocation
    void innerUpdateBossToOnline(PlayerPacket packet, long... exceptRoleIds);

    /**
     * 定时检测boss状态,改变推送给客户端(异步)
     */
    @AsyncInvocation
    public void checkBossStatus();

    /**
     * 排行榜遍历,更新排名(只排当前召唤boss)
     */
    @AsyncInvocation
    public void rankSort();
    
    /**
     * 获得存活boss
     * 红点用
     */
    public Set<Integer> getAliveBossIds(long roleId);
}
