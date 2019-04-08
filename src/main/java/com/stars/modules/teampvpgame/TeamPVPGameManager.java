package com.stars.modules.teampvpgame;

import com.stars.modules.teampvpgame.prodata.DoublePVPConfigVo;
import com.stars.modules.teampvpgame.prodata.DoublePVPRewardVo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/12/15.
 */
public class TeamPVPGameManager {
    public static byte minMemberCount = 2;// 队伍最小人数
    public static byte maxMemberCount = 2;// 队伍最大人数

    // 等级浮动
    public static int levelExcursion;
    // 报名确认时间
    public static long signUpConfirmTime;
    // 积分赛排行榜上限
    public static int scoreRankLimit;

    // 产品数据
    // 组队pvp奖励
    public static Map<Integer, List<DoublePVPRewardVo>> pvpRewardVoMap = new HashMap<>();
    // 组队pvp配置;
    public static Map<Byte, DoublePVPConfigVo> pvpConfigVoMap = new HashMap<>();

    public static DoublePVPConfigVo getConfigVo(byte tpgType) {
        return pvpConfigVoMap.get(tpgType);
    }

    public static DoublePVPRewardVo getTPGReward(int type, int rank) {
        if (!pvpRewardVoMap.containsKey(type)) {
            return null;
        }
        for (DoublePVPRewardVo rewardVo : pvpRewardVoMap.get(type)) {
            if (rewardVo.getRankArray()[0] <= rank && rank <= rewardVo.getRankArray()[1]) {
                return rewardVo;
            }
        }
        return null;
    }
}
