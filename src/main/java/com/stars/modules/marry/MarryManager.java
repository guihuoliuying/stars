package com.stars.modules.marry;

import com.stars.modules.marry.prodata.MarryActivityVo;
import com.stars.modules.marry.prodata.MarryBattleScoreVo;
import com.stars.modules.marry.prodata.MarryRing;
import com.stars.modules.marry.prodata.MarryRingLvl;
import com.stars.modules.serverLog.EventType;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhouyaohui on 2016/12/1.
 */
public class MarryManager {

    // 表白类型
    public static final byte PROFRESS_SEND = 1;     // 表白已发送
    public static final byte PROFRESS_SUCCESS = 2;  // 同意表白
    public static final byte PROFRESS_FAILED = 3;   // 表白失败
    public static final byte PROFRESS_OUTTIME = 4;   // 表白过期
    public static final byte OTHER_NOT_SINGLE = 5;   // 对方已不是单身

    public static final byte WAY_CLAIM = 0;     // 宣言途径
    public static final byte WAY_FRIEND = 1;    // 好友途径

    public final static byte BREAK_BYE = 0;     // 分手
    public static final byte BREAK_GENERAL = 1; // 协商决裂
    public static final byte BREAK_FORCE = 2;   // 强制决裂
    public final static byte BREAK_AGREE = 3;   // 同意决裂
    public final static byte BREAK_REFUSE = 4;  // 拒绝决裂

    public final static byte BREAK_STATE_FRIENDLY = 0;  // 友好状态，没有提出过决裂
    public final static byte BREAK_STATE_PROPOSE = 1;   // 提出决裂
    public final static byte BREAK_STATE_OVER = 2;      // 决裂状态
    public final static byte BREAK_STATE_REFUSE = 3;    // 拒绝决裂

    // 资源道具操作的一些常量
    public final static byte TOOL_OPERATOR_BREAK_GENERAL = 1;  // 普通决裂消耗
    public final static byte TOOL_OPERATOR_BREAK_FORCE = 2;    // 强制决裂消耗
    public final static byte TOOL_OPERATOR_CANDY_REWARD = 3;    // 喜糖奖励
    public final static byte TOOL_OPERATOR_FIREWORKS = 4;   // 烟花消耗
    public final static byte TOOL_OPERATOR_FIREWORKS_AWARD = 5; // 烟花奖励
    public final static byte TOOL_OPERATOR_REDBAG_SEND = 6; // 发红包消耗
    public final static byte TOOL_OPERATOR_REDBAG_SENDA_AWARD = 7;  // 发红包奖励
    public final static byte TOOL_OPERATOR_REDBAG_GET = 8; // 抢红包奖励
    public final static byte TOOL_OPERATOR_PROFRESS = 9;    // 表白消耗
    public final static byte TOOL_OPERATOR_APPOINT_LUXURIOUS = 10;  // 豪华预约消耗
    public final static byte TOOL_OPERATOR_APPOINT_GENERAL = 11;    // 普通预约消耗
    public final static byte TOOL_OPERATOR_APPOINT_AWARD = 12;      // 普通预约奖励
    public final static byte TOOL_OPERATOR_APPOINT_LUXURIOUS_AWARD = 13;    // 豪华预约奖励
    public final static byte TOOL_APPOINT_RETURN_COST = 14;    // 普通预约拒绝返还
    public final static byte TOOL_APPOINT_LUXURIOUS_RETURN_COST = 15;    // 豪华预约拒绝返还

    // 婚礼类型
    public static final byte GENERAL_WEDDING = 0;   // 普通婚礼
    public static final byte LUXURIOUS_WEDDING = 1; // 豪华婚礼
    public static final byte AGREE_WEDDING = 2;     // 同意预约
    public static final byte REFUSE_WEDDING = 3;    // 拒绝预约
    public static final byte LUXURIOUS_AGREE_WEDDING = 4;     // 豪华同意预约
    public static final byte LUXURIOUS_REFUSE_WEDDING = 5;    // 豪华拒绝预约


    // 活动类型
    public final static int ACTIVITY_REDBAG = 1;    // 红包活动
    public final static int ACTIVITY_FIREWORKS = 2; // 烟花活动
    public final static int ACTIVITY_CANDY = 3; // 喜糖活动

    // 喜糖活动
    public final static byte CANDY_ACTIVITY_BEGIN = 1;   // 喜糖活动开始
    public final static byte CANDY_ACTIVITY_CLICK = 2;  // 喜糖被点击

    // 红包活动
    public final static byte REDBAG_ACTIVITY_SEND = 1;  // 发红包
    public final static byte REDBAG_ACTIVITY_GET = 2;   // 抢红包

    public static int BEGINATTENTION = 300;     // 豪华婚礼准备时间
    public static int ATTENTION_INTERVAL = 60;  // 豪华婚礼间隔提醒
    public static int CANDY_DELAY = 0;  // 喜糖cd
    public static int FIREWORKS_DELAY = 0;  // 烟花cd
    public static int REDBAG_DELAY = 0;     // 红包cd

    public static int MARRY_LOVEINFO_HOLDTIME_PUBLIC;   // 宣言过期时间
    public static int TEAM_DUNGEON_COUNT = 100;

    public static byte MAN = 1;
    public static byte WOMAN = 2;

    public static int MAX_PROFRESS_LIMIT = 100;
    public static int CONFIG_PROFRESS_OUTTIME = 24 * 3600;

    public static ConcurrentMap<Byte, EventType> toolEventMap = new ConcurrentHashMap<>();

    public static void registEvent() {
        toolEventMap.put(TOOL_OPERATOR_BREAK_GENERAL,EventType.BREAK_GENERAL);
        toolEventMap.put(TOOL_OPERATOR_BREAK_FORCE,EventType.BREAK_FORCE);
        toolEventMap.put(TOOL_OPERATOR_CANDY_REWARD,EventType.CANDY_REWARD);
        toolEventMap.put(TOOL_OPERATOR_FIREWORKS,EventType.FIREWORKS_COST);
        toolEventMap.put(TOOL_OPERATOR_FIREWORKS_AWARD,EventType.FIREWORKS_AWARD);
        toolEventMap.put(TOOL_OPERATOR_REDBAG_SEND,EventType.REDBAG_SEND_COST);
        toolEventMap.put(TOOL_OPERATOR_REDBAG_SENDA_AWARD,EventType.REDBAG_SENDA_AWARD);
        toolEventMap.put(TOOL_OPERATOR_REDBAG_GET,EventType.REDBAG_GET);
        toolEventMap.put(TOOL_OPERATOR_PROFRESS,EventType.PROFRESS_COST);
        toolEventMap.put(TOOL_OPERATOR_APPOINT_LUXURIOUS,EventType.APPOINT_LUXURIOUS_COST);
        toolEventMap.put(TOOL_OPERATOR_APPOINT_GENERAL,EventType.APPOINT_GENERAL_COST);
        toolEventMap.put(TOOL_OPERATOR_APPOINT_AWARD,EventType.APPOINT_AWARD);
        toolEventMap.put(TOOL_OPERATOR_APPOINT_LUXURIOUS_AWARD,EventType.APPOINT_LUXURIOUS_AWARD);
        toolEventMap.put(TOOL_APPOINT_RETURN_COST,EventType.APPOINT_RETURN_COST);
        toolEventMap.put(TOOL_APPOINT_LUXURIOUS_RETURN_COST,EventType.APPOINT_LUXURIOUS_RETURN_COST);
    }

    //戒指位置
    public static int RING_POS1 = 1;

    public static int MAX_PROFRESSED = 200;

    public static int MAX_WEDDING = 100;

    public static ConcurrentMap<Integer, MarryRing> ringMap = new ConcurrentHashMap<>();

    public static ConcurrentMap<Integer, MarryActivityVo> activityMap = new ConcurrentHashMap<>();

    public static ConcurrentMap<String, MarryRingLvl> ringLvMap = new ConcurrentHashMap<>();

    public static ConcurrentMap<Integer, MarryBattleScoreVo> marryBattleScoreVoMap = new ConcurrentHashMap<>();

    public static MarryActivityVo getMarryActivityVo(int type) {
        return activityMap.get(type);
    }

    public static MarryRing getMarryRingVo(int id) {
        return ringMap.get(id);
    }

    public static MarryRingLvl getMarryRingLvVo(int id , short level) {
        return ringLvMap.get(id + "_" + level);
    }

    public static MarryBattleScoreVo getMarryBattleVo(int id){
        return marryBattleScoreVoMap.get(id);
    }

}
