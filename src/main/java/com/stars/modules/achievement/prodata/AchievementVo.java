package com.stars.modules.achievement.prodata;

import com.stars.modules.achievement.AchievementManager;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhouyaohui on 2016/10/17.
 */
public class AchievementVo {
    private int achievementId;  // 成就id
    private int stage;         // 成就所属阶段
    private String icon;        // 图标
    private String describe;    // 描述
    private String name;        // 名字
    private int rank;           // 排序
    private int type;           // 类型
    private String func;    // 功能
    private String award;       // 奖励
    private int showexpressId;  // 特效
    private int achievementCount; //成就完成能获得的成就点
    private String openWindow; //快速跳转
    private String limit; //条件限制
    private Map<Byte,Integer> limitMap = new HashMap<>();

    public int getShowexpressId() {
        return showexpressId;
    }

    public void setShowexpressId(int showexpressId) {
        this.showexpressId = showexpressId;
    }

    public int getAchievementId() {
        return achievementId;
    }

    public void setAchievementId(int achievementId) {
        this.achievementId = achievementId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFunc() {
        return func;
    }

    public void setFunc(String func) {
        this.func = func;
    }

    public String getAward() {
        return award;
    }

    public void setAward(String award) {
        this.award = award;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public int getAchievementCount() {
        return achievementCount;
    }

    public void setAchievementCount(int achievementCount) {
        this.achievementCount = achievementCount;
    }

    public String getOpenWindow() {
        return openWindow;
    }

    public void setOpenWindow(String openWindow) {
        this.openWindow = openWindow;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
        if(limit.equals("0"))
            return;
        this.limitMap = StringUtil.toMap(limit,Byte.class,Integer.class,'+','|');
    }

    public boolean matchLimit(int roleLevel, int vipLevel){
        if(StringUtil.isEmpty(limitMap) ||limit.equals("0"))
            return true;
        int reqRoleLevel = 0;
        int reqRoleVipLevel = 0;
        if(limitMap.get(AchievementManager.CONDITION_ROLE_LEVEL) != null)
            reqRoleLevel = limitMap.get(AchievementManager.CONDITION_ROLE_LEVEL);
        if(limitMap.get(AchievementManager.CONDITION_VIP_LEVEL) != null)
            reqRoleVipLevel = limitMap.get(AchievementManager.CONDITION_VIP_LEVEL);
        return roleLevel >= reqRoleLevel && vipLevel >= reqRoleVipLevel;
    }
}
