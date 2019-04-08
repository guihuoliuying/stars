package com.stars.modules.role;

import com.stars.modules.role.prodata.FightScoreRewardVo;
import com.stars.modules.role.prodata.Grade;
import com.stars.modules.role.prodata.Job;
import com.stars.modules.role.prodata.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/5/13.
 */
public class RoleManager {
    /**
     * 角色属性组成部分key
     */
    public static String ROLEATTR_GRADEBASE = "gradebase";// 产品数据配置
    public static String ROLEATTR_TITLE = "title";// 称号属性
    public static String ROLEATTR_EQUIPMENT = "equipment";//装备属性;
    public static String ROLEATTR_SKILL = "skill";//技能效果
    public static String ROLEATTR_BUDDYLINEUP = "buddylineup";// 伙伴阵型
    public static String ROLEATTR_TRUMP = "trump";  // 法宝属性
    public static String ROLEATTR_GEM = "gem";//宝石
    public static String ROLEATTR_MARRYRING = "marryring"; //婚戒
    public static String ROLEATTR_BOOK = "book";    // 典籍
    public static String ROLEATTR_CAMP = "camp";    // 阵营
    public static String ROLEATTR_BABY = "baby";//宝宝系统
    public static String ROLEATTR_SOUL = "soul";//元神系统
    /**
     * 战力组成部分key
     */
    public static String FIGHTSCORE_GRADE = "grade";// 角色等级
    public static String FIGHTSCORE_EQUIPMENT = "equipment";// 装备
    public static String FIGHTSCORE_GEM = "gem";// 宝石
    public static String FIGHTSCORE_TITLE = "title";// 称号
    public static String FIGHTSCORE_SKILL = "skill";//技能
    public static String FIGHTSCORE_TRUMP = "trump";    // 法宝
    public static String FIGHTSCORE_MARRYRING = "marryring";//婚戒
    public static String FIGHTSCORE_BOOK = "book";   // 典籍
    public static String FIGHTSCORE_CAMP = "camp";    // 阵营
    public static String FIGHTSCORE_DAILY = "daily";    //日常（斗魂珠）
    public static String FIGHTSCORE_BABY = "baby";//宝宝系统
    public static String FIGHTSCORE_SOUL = "soul";//元神系统

    public static int ROLE_LEVEL_INIT = 1;// 玩家初始等级
    public static int ROLE_EXP_INIT = 0;// 玩家初始经验
    // 职业最大等级 jobId-maxLevel
    public static Map<Integer, Integer> maxLevelMap = new HashMap<>();
    //玩家初始装备;job-<equipmentType,equipmentId>
    public static Map<Integer, Map<Byte, Integer>> bornEquipmentMap = null;
    //玩家等级信息产品数据;
    //<job, <level, Grade>>
    public static Map<Integer, Map<Integer, Grade>> GradeMap = new HashMap<>();
    public static Map<Integer, Job> jobMap = new HashMap<>();
    public static Map<Integer, Resource> resourceMap = new HashMap<>();

    /* 战力产品数据 */
    public static Map<Integer, FightScoreRewardVo> fightScoreRewardVoMap = new HashMap<>();// id-vo

    /* 体力恢复相关 */
    public static int VIGOR_RECOVERY_INTERVAL = 360 * 1000; // 体力恢复时间
    public static int VIGOR_RECOVERY_NUMBER = 1; // 体力恢复量

    /* 体力购买相关 */
    public static Map<Integer, int[]> vigorPriceMap; // times -> [itemId, price, buyNumber]
    public static int buyVigorLimit;// 体力购买上限(总次数需要加上vip)

    /* 金币购买相关 */
    public static int BUY_MONEY_MULTI = 10;// 批量购买金币次数


    public static byte MaxRoleResourceCount = 63;


    /*体力可以存储的最大上限值*/
    public static int canSaveMaxVigor = 0;

    /**
     * 设置出生时的装备
     */
    public static void setBornEquipmentMap(Map<Integer, Map<Byte, Integer>> map) {
        bornEquipmentMap = map;
    }

    /**
     * 获取对应职业的对应装备位的装备ID;
     *
     * @param jobId
     * @param equipmentType
     * @return
     */
    public static Integer getBornEquipmentId(int jobId, byte equipmentType) {
        Map<Byte, Integer> map = bornEquipmentMap.get(jobId);
        return map.get(equipmentType);
    }

    /**
     * 缓存产品数据,用于缓存角色升级的一些产品数据;
     **/
    public static void setGradeDatas(List<Grade> list_) {
        Map<Integer, Map<Integer, Grade>> gradeMap = new HashMap<>();
        Map<Integer, Integer> maxLevelMap = new HashMap<>();

        for (int i = 0, len = list_.size(); i < len; i++) {
            Grade tmpGrade = list_.get(i);
            if (gradeMap.containsKey(tmpGrade.getJob()) == false) {
                gradeMap.put(tmpGrade.getJob(), new HashMap<Integer, Grade>());
            }
            gradeMap.get(tmpGrade.getJob()).put(tmpGrade.getLevel(), tmpGrade);
            // 注入职业最大等级 map
            Integer maxLevel = maxLevelMap.get(tmpGrade.getJob());
            if (maxLevel == null || tmpGrade.getLevel() > maxLevel) {
                maxLevelMap.put(tmpGrade.getJob(), tmpGrade.getLevel());
            }
        }

        RoleManager.GradeMap = gradeMap;
        RoleManager.maxLevelMap = maxLevelMap;
    }

    public static void setJobDatas(Map<Integer, Job> jobMap_) {
        jobMap = jobMap_;
    }

    public static void setResourceDatas(List<Resource> resourceList) {
        Map<Integer, Resource> resourceMap = new HashMap<>();
        for (int i = 0, len = resourceList.size(); i < len; i++) {
            Resource resource = resourceList.get(i);
            resourceMap.put(resource.getId(), resource);
        }

        RoleManager.resourceMap = resourceMap;
    }

    /**
     * 获取对应的职业产品数据;
     **/
    public static Job getJobById(int jobId_) {
        return jobMap.get(jobId_);
    }

    /**
     * 获取对应的资源产品数据;
     **/
    public static Resource getResourceById(int resourceId_) {
        return resourceMap.get(resourceId_);
    }

    /**
     * 获取Grade, 可以知道对应职业的对应等级的一些基础属性值;
     **/
    public static Grade getGradeByJobLevel(int job_, int level_) {
        if (GradeMap.containsKey(job_)) {
            return GradeMap.get(job_).get(level_);
        }
        return null;
    }

    /**
     * 获取升级到level_所需经验;
     **/
    public static int getRequestExpByJobLevel(int job_, int level_) {
        Grade tmpGrade = getGradeByJobLevel(job_, level_);
        if (tmpGrade != null) {
            return tmpGrade.getReqexp();
        }
        return -1;
    }

    public static int getMaxlvlByJobId(int jobId) {
        return maxLevelMap.get(jobId);
    }

    public static FightScoreRewardVo getFightScoreRewardVo(int rewardId) {
        return fightScoreRewardVoMap.get(rewardId);
    }

    public static int getBuyVigorLimit() {
        return buyVigorLimit;
    }
}
