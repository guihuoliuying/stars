package com.stars.modules.fightingmaster;

import com.stars.modules.fightingmaster.prodata.PersonPKcoeVo;
import com.stars.modules.fightingmaster.prodata.PersonPaircoeVo;
import com.stars.multiserver.fightingmaster.Matcher;

/**
 * Created by zhouyaohui on 2016/11/7.
 */
public class FightingMasterManager {

    public volatile static Matcher<PersonPKcoeVo> pkVo = new Matcher<>();
    public volatile static Matcher<PersonPaircoeVo> pairVo = new Matcher<>();
    public volatile static int stageId = 999;   // stageId
    public volatile static int fighttime = 60;  // 战斗时长
    public volatile static int pairmatchcount;  // 首次参加斗神殿，匹配机器人的次数
    public volatile static int pairmatchPersent;    // 首次参加斗神殿，匹配机器人的战力范围
    public volatile static int winProtectScore; // 胜利保守积分
    public volatile static int loseProtectScore;    // 失败保守积分
    public volatile static int maxDisSegmentRankId;  // 最高积分段排行榜id
    public volatile static int maxDisScore;         // 最高积分段最低积分
    public volatile static int rankSize = 100;      // 排行榜大小
    public volatile static int rankReset = 0;          // 排行榜重置
    public volatile static int dailyReset = 0;      // 每日次数重置
    public volatile static int awardCount = 0;      // 每日胜利有奖励次数


    public static PersonPKcoeVo matchVo(int fightScore, int disScore) {
        PersonPKcoeVo match = new PersonPKcoeVo();
        match.setCminPower(fightScore);
        match.setCmaxPower(fightScore);
        match.setCminScore(disScore);
        match.setCmaxScore(disScore);
        return pkVo.match(match, 0);
    }

    public static PersonPaircoeVo matchPairVo(int disScore) {
        PersonPaircoeVo match = new PersonPaircoeVo();
        match.setCminScore(disScore);
        match.setCmaxScore(disScore);
        return pairVo.match(match, 0);
    }
}
