package com.stars.services.rank;

import com.stars.services.Service;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;
import com.stars.core.actor.invocation.annotation.Timeout;

import java.util.List;

/**
 * Created by liuyuheng on 2016/8/22.
 */
public interface RankService extends ActorService, Service {
    /**
     * 上线推送/更新推送
     *
     * @param abstractRankPo
     */
    @AsyncInvocation
    void updateRank(byte rankType, AbstractRankPo abstractRankPo);

    @AsyncInvocation
    void updateRank4BestCP(byte rankType, AbstractRankPo rankPo, int cpId);

    @AsyncInvocation
    void updateRank4CampCity(byte rankType, AbstractRankPo rankPo, int newcityId, int oldCityId);

    void updateRank4DragonBoat(byte rankType, AbstractRankPo rankPo, Long stageTime);

    /**
     * 获取端午节赛龙舟所有排行榜时间阶段
     *
     * @param rankId
     * @return
     */
    List<Long> getAllStageTimeList4DragonBoat(int rankId);

    @AsyncInvocation
    void removeRank(int rankId, long uniqueId, AbstractRankPo abstractRankPo);

    /**
     * 发奖
     * 每日/每周发奖都走这里
     *
     * @param type
     */
    @AsyncInvocation
    void rewardHandler(byte type);

    /**
     * 下线的时候清除角色排名
     *
     * @param roleId
     */
    @AsyncInvocation
    void offline(byte rankType, long uniqueId);

    /**
     * 保存
     */
    @AsyncInvocation
    void save();


    /**
     * 获得排行榜,直接下发客户端,默认最后一个是自己的数据
     *
     * @param roleId
     * @param rankId
     */
    @AsyncInvocation
    void sendRankList(int rankId, long uniqueId);


    void sendRankList(int rankId, long roleId, long familyId);

    /**
     * 下发最佳组合投票者排行榜
     *
     * @param rankId
     * @param uniqueId
     * @param args
     */
    @AsyncInvocation
    void sendRankList4BestCPVoter(int rankId, long uniqueId, int cpId);

    /**
     * 下发赛龙舟排行榜
     *
     * @param rankId
     * @param uniqueId
     */
    void sendRankList4DragonBoat(int rankId, long uniqueId);

    /**
     * 获取排名信息(可能会有业务需要)
     *
     * @param roleId
     * @param rankId
     * @return
     */
    AbstractRankPo getRank(int rankId, long uniqueId);

    AbstractRankPo getRank(int rankId, long uniqueId, Object... args);

    /**
     * 根据排名匹配(可能会有业务需要)
     *
     * @param roleId
     * @param rankId
     * @param section
     * @return
     */
    long getRankMatching(long roleId, int rankId, int section);

    /**
     * 获得排行榜前n名
     *
     * @param rankId
     * @param frontCount
     * @return
     */
    @Timeout(timeout = 10_000)
    List<AbstractRankPo> getFrontRank(int rankId, int frontCount);

    /**
     * 获得排行榜前n名
     *
     * @param rankId
     * @param frontCount
     * @return
     */
    @Timeout(timeout = 30_000)
    List<AbstractRankPo> getFrontRank(int rankId, int frontCount, Object... args);

    /**
     * 每日重置
     */
    @AsyncInvocation
    void dailyReset();

    /**
     * 遍历排名
     */
    @AsyncInvocation
    void sortRank();

    /**
     * 指定时间发奖检查
     */
    @AsyncInvocation
    void appointReward();

    List<AbstractRankPo> getRankingList(int rankId);

    @AsyncInvocation
    void flushFightScore2FightingMaster();

    @AsyncInvocation
    void updateRoleName(long roleId, String newName);

    @AsyncInvocation
    void resetRank4Camp();
}

