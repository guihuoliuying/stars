package com.stars.modules.achievement;

import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;

/**
 * Created by zhouyaohui on 2016/10/18.
 */
public interface AchievementHandler {
    /** 成就类型 */
    int TYPE_TOOL = 1;      // 收集道具
    int TYPE_ACTIVITY = 2;  // 参加活动
    int TYPE_BUDDY = 3;     // 伙伴
    int TYPE_RIDE = 4;      // 坐骑
    int TYPE_EQUIP = 5;     // 装备
    int TYPE_GEM = 6;       // 宝石
    int TYPE_PART = 7;      // 部位
    int TYPE_ROLELEVEL = 8;     // 角色等级
    int TYPE_FAMILY = 9;    // 家族
    int TYPE_SKILL = 10;    // 技能
    int TYPE_FIGHTING = 11; // 战力
    int TYPE_SWEEP = 12;    // 扫荡
    int TYPE_LOGIN = 13;    // 连续登陆
    int TYPE_DEITY = 15;    // 神兵
    int TYPE_DUNGEON = 17;  // 通关星星数
    int TYPE_CHAPTER = 18;  // 章节
    int TYPE_WASH = 21; // 洗练
    int TYPE_GUEST = 24;    // 门客
    int TYPE_FRIEND = 25;    // 好友
    int TYPE_TRUMP = 26;    //法宝
    int TYPE_BOOK = 27;     //典籍
    int TYPE_DAILY5V5 = 28;     //日常5V5
    int TYPE_ELITE_DUNGEON = 29;    //精英副本
    int TYPE_SIGNIN = 30;   //签到
    int TYPE_TITLE = 31;    //称号
    int TYPE_CHANGEJOB = 32;    //转职
    /**
     *
     * @param row
     * @param vo
     * @param condition
     * @return  返回是否修改过row true 更新过 false 没有更新
     */
    boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception;
}
