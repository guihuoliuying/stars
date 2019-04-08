package com.stars.modules.newserverfightscore;

import com.stars.modules.newserverfightscore.prodata.NewServerFightScoreVo;

import java.util.Map;

/**
 * Created by liuyuheng on 2017/1/4.
 */
public class NewServerFightManager {
    // 奖励类型
    public static final byte REWARD_TYPE_FIGHTSCORE = 1;// 战力值
    public static final byte REWARD_TYPE_RANKING = 2;// 战力排名
    // 战力值补发邮件模板id
    public static int FIGHTSCORE_EMAIL_TEMPLATE = 23001;
    // 战力排名补发奖励邮件模板id
    public static int RANKING_EMAIL_TEMPLATE = 26006;
    // 邮件发送名称
    public static String SENDER_NAME = "活动管理员";
    // 领取奖励状态
    public static final byte REWARD_STATUS_NOTREACHTIME = 1;// 未到时间
    public static final byte REWARD_STATUS_NOTREACHCONDITION = 2;// 未达到条件
    public static final byte REWARD_STATUS_CANREWARD = 3;// 可领取
    public static final byte REWARD_STATUS_REWARDED = 4;// 已领取

    // <void, vo>
    public static Map<Integer, Map<Integer, NewServerFightScoreVo>> NSFightScoreVoMap;

    public static NewServerFightScoreVo getNSFightScoreVo(int operateActId, int nsFightScoreId) {
        if (!NSFightScoreVoMap.containsKey(operateActId))
            return null;
        return NSFightScoreVoMap.get(operateActId).get(nsFightScoreId);
    }

    public static Map<Integer, NewServerFightScoreVo> getNSFSVoMap(int operateActId) {
        return NSFightScoreVoMap.get(operateActId);
    }
}
