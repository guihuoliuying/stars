package com.stars.modules.dragonboat;

import com.stars.modules.dragonboat.prodata.DragonBoatVo;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by huwenjun on 2017/5/9.
 */
public class DragonBoatManager {
    /**
     * <dragonboatid,Dragonboat>
     */
    public static Map<Integer, DragonBoatVo> dragonBoatMap;
    /**
     * 活动开始时间
     */
    public static Date beginDateTime;
    /**
     * 活动结束时间
     */
    public static Date endDateTime;
    /**
     * 加速道具
     */
    public static int speedUpItemId;
    /**
     * 减速道具
     */
    public static int speedDownItemId;
    /**
     * 最大下注次数
     */
    public static int maxSelectTimes;
    /**
     * 有效活动id集合
     */
    public static Set<Integer> avaliableActivityIds;
    /**
     * 日活动流程
     * dayStep,cron
     */
    public static Map<Integer, String> dayFlowMap;
    /**
     * 日活动
     * dayStep,ActivityId
     */
    public static Map<Integer, Integer> dayActivityMap;
    /**
     * 排序后的龙舟列表
     */
    public static List<DragonBoatVo> dragonBoats;
    /**
     * <rank,reqard>
     * 排行榜奖励
     */
    public static Map<Integer, String> rankRewardMap;
    /**
     * 每天活动时间排列
     * <时间戳,环节数>
     */
    public static Map<Long, Integer> dayActivityTimeMap;
    /**
     * 伪加速范围
     */
    public static Integer[] dragonboatSpeedupRandomranges;
    /**
     * 伪加速范围
     */
    public static Integer[] dragonboatSlowdownRandomranges;




}


