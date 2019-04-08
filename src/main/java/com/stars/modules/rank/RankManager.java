package com.stars.modules.rank;

import com.stars.services.rank.prodata.RankAwardVo;
import com.stars.services.rank.prodata.RankDisplayVo;

import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/10/27.
 */
public class RankManager {
    /* 产品数据 */
    public static Map<Integer, RankDisplayVo> rankDisplayVoMap;
    public static Map<Integer, List<RankAwardVo>> rankRewardVoMap;
    public static Map<Byte, List<Integer>> rewardTypeMap;// <奖励类型type,rankIdList>

    //这两个是临时的，测试完就没用了
    public static int roleRankNum = Integer.MAX_VALUE;
    public static int familyRankNum = Integer.MAX_VALUE;

    public static List<Integer> getRankIdByRewardType(byte type) {
        return rewardTypeMap.get(type);
    }

    public static RankDisplayVo getRankDisplayVo(int rankId) {
        return rankDisplayVoMap.get(rankId);
    }

    public static List<RankAwardVo> getRankAward(int rankId) {
        return rankRewardVoMap.get(rankId);
    }

    public static RankAwardVo getRankAwardVo(int rankId, int rank) {
        List<RankAwardVo> voList = getRankAward(rankId);
        for (RankAwardVo vo : voList) {
            if (vo.isInSectionRange(rank)) {
                return vo;
            }
        }
        return null;
    }
}
