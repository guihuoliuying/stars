package com.stars.modules.mooncake;

import com.stars.modules.mooncake.prodata.moonCakeRwdVo;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangerjiang on 2017/9/14.
 */
public class MoonCakeManager {
    public static int iLastTime = 0;//每次游戏持续时间，即单场游戏总时间
    public static int iRedTime = 0; //每次游戏快结束时，时间变红时间开始点
    public static int iCanGetMaxScore = 0;//单局游戏最大积分值，用于排除外挂加速情况
    public static int MAX_TIME;//单局时间上限


    public static Map<Integer, moonCakeRwdVo> dayScoreRwdMap = new HashMap<>(); //key积分，value道具Map

    public static moonCakeRwdVo getDayScoreRwdMap(int score) {
        if (StringUtil.isEmpty(dayScoreRwdMap)) {
            return null;
        } else {
            return dayScoreRwdMap.get(score);
        }
    }
}
