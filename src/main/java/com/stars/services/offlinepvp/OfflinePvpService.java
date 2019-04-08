package com.stars.services.offlinepvp;

import com.stars.services.Service;
import com.stars.services.offlinepvp.cache.OPEnemyCache;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/10/8.
 */
public interface OfflinePvpService extends Service, ActorService {
    /**
     * 更新等级和战力,用于明天的基准等级和战力
     *
     * @param roleId
     * @param level
     * @param fightScore
     */
    @AsyncInvocation
    void updateUseStandard(long roleId, int level, int fightScore);

    /**
     * 获得当日使用匹配id和奖励id
     *
     * @param roleId
     * @param jobId
     * @return
     */
    public int[] getMatchAndRewardId (long roleId, int jobId);

    /**
     * 获得匹配对手
     *
     * @param roleId
     * @param matchVoId
     * @param standardLevel
     * @return
     */
    Map<Byte, OPEnemyCache> getMatchEnemys(long roleId, int matchVoId, int standardLevel);

    /**
     * 刷新匹配对手
     *
     * @param roleId
     * @param matchVoId
     * @param standardLevel
     * @return
     */
    Map<Byte, OPEnemyCache> refreshMatchEnemys(long roleId, int matchVoId, int standardLevel);

    /**
     * 根据等级和职业获得匹配结果
     *
     * @param matchLevel
     * @param jobId
     * @param count
     * @param exception 过滤roleId
     * @return
     */
    List<OPEnemyCache> executeMatch(int matchLevel, int jobId, int count, List<Long> exception);

    /**
     * 每日重置(重置使用基准战力)
     */
    @AsyncInvocation
    void dailyReset();
}
