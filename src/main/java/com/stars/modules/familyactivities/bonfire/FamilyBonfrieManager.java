package com.stars.modules.familyactivities.bonfire;

import com.stars.modules.familyactivities.bonfire.prodata.FamilyFireVo;
import com.stars.modules.familyactivities.bonfire.prodata.FamilyQuestion;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wuyuxing on 2017/3/7.
 */
public class FamilyBonfrieManager {
    public static Map<Integer, FamilyFireVo> FAMILY_FIRE_MAP = new HashMap<>();
    public static Map<Integer, FamilyQuestion> FAMILY_QUESTION_MAP = new HashMap<>();

    public static int DEFAULT_LEVEL;     //初始等级
    public static int DEFAULT_EXP;       //初始经验
    public static int WOOD_ID;           //干柴Id
    public static int WOOD_REFRESH_CD;   //干柴刷新间隔
    public static int RED_EXP;           //红包经验
    public static int WOOD_EXP;          //干柴经验
    public static int GOLD_EXP;          //元宝经验
    public static int WOOD_DROP_GROUP;   //投干柴掉落组
    public static int GOLD_DROP_GROUP;   //投元宝掉落组
    public static int GOLD_COUNT = 1;        //每次投掷元宝数量
    public static int QUESTIONS_INTERVAL;  //题目刷新间隔
    public static int QUESTIONS_COUNT;     //题目数量
    public static int DAILY_THROW_GOLD_COUNT;     //每日投元宝次数限制


    public static Map<Integer, FamilyFireVo> getFamilyFireMap() {
        return FAMILY_FIRE_MAP;
    }

    public static void setFamilyFireMap(Map<Integer, FamilyFireVo> familyFireMap) {
        FAMILY_FIRE_MAP = familyFireMap;
    }

    public static Map<Integer, FamilyQuestion> getFamilyQuestionMap() {
        return FAMILY_QUESTION_MAP;
    }

    public static FamilyQuestion getQuestion(int id){
        if(FAMILY_QUESTION_MAP==null) return null;
        return FAMILY_QUESTION_MAP.get(id);
    }

    public static void setFamilyQuestionMap(Map<Integer, FamilyQuestion> familyQuestionMap) {
        FAMILY_QUESTION_MAP = familyQuestionMap;
    }

    public static FamilyFireVo getFireVo(int level){
        return FAMILY_FIRE_MAP.get(level);
    }

    public static int getDailyThrowGoldCount() {
        return DAILY_THROW_GOLD_COUNT;
    }
}
