package com.stars.modules.buddy;

import com.stars.modules.buddy.prodata.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/8/5.
 */
public class BuddyManager {
    /* 常量 */
    public static byte LINEUP_START = 1;// 阵型起始编号
    public static byte LINEUP_END = 7;// 阵型结束编号
    public static byte BUDDY_NOT_FOLLOW = 0;// 伙伴不跟随
    public static byte BUDDY_FOLLOW = 1;// 伙伴跟随
    public static byte BUDDY_NOT_FIGHT = 0;// 伙伴不出战
    public static byte BUDDY_FIGHT = 1;// 伙伴出战
    public static int BUDDY_INIT_LEVEL = 1;// 伙伴初始等级
    public static int BUDDY_INIT_STAGELV = 1;// 初始阶级
    public static int BUDDY_INIT_ARMLV = 1;// 初始武装等级
    public static byte BUDDY_EQUIP_NOT_PUTON = 0;// 未装备
    public static byte BUDDY_EQUIP_PUTON = 1;// 已装备

    // 伙伴基础数据 <id, vo>
    public static Map<Integer, BuddyinfoVo> buddyinfoVoMap = new HashMap<>();
    // 等级数据 <id, <levle, vo>
    public static Map<Integer, Map<Integer, BuddyLevelVo>> buddyLevelVoMap = new HashMap<>();
    // 阶级数据 <id, <stagelevel, vo>>
    public static Map<Integer, Map<Integer, BuddyStageVo>> buddyStageVoMap = new HashMap<>();
    // 阵型数据 <lineupid, <armlevel, vo>>
    public static Map<Byte, Map<Integer, BuddyLineupVo>> lineupLevelMap = new HashMap<>();
    // 武装数据 <id, <armlevel, vo>>
    public static Map<Integer, Map<Integer, BuddyArmsVo>> buddyArmsVoMap = new HashMap<>();
    public static int expItemId;//加伙伴经验道具itemid
    public static int expUnit;//每个道具添加的经验值数量
    public static Map<Integer, List<BuddyGuard>> buddyGuardGroupMap;//伙伴守卫
    public static Map<Integer, BuddyGuard> buddyIdGuardMap;//伙伴id和组映射
    public static Map<Integer, String> buddyGuardGroupIdNameMap;//组名称映射

    public static BuddyLevelVo getBuddyLevelVo(int buddyId, int level) {
        if (!buddyLevelVoMap.containsKey(buddyId)) {
            return null;
        }
        return buddyLevelVoMap.get(buddyId).get(level);
    }

    public static BuddyStageVo getBuddyStageVo(int buddyId, int stageLevel) {
        if (!buddyStageVoMap.containsKey(buddyId)) {
            return null;
        }
        return buddyStageVoMap.get(buddyId).get(stageLevel);
    }

    public static BuddyLineupVo getBuddyLineupVo(byte lineupId, int armlv) {
        if (!lineupLevelMap.containsKey(lineupId))
            return null;
        return lineupLevelMap.get(lineupId).get(armlv);
    }

    public static BuddyinfoVo getBuddyinfoVo(int buddyId) {
        return buddyinfoVoMap.get(buddyId);
    }

    public static BuddyArmsVo getBuddyArmVo(int buddyId, int armLevel) {
        if (!buddyArmsVoMap.containsKey(buddyId)) {
            return null;
        }
        return buddyArmsVoMap.get(buddyId).get(armLevel);
    }
}
