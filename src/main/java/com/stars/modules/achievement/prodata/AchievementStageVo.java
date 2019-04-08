package com.stars.modules.achievement.prodata;

import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/8/7.
 */
public class AchievementStageVo {
    private int stage;  //成就阶段
    private String name; //阶段名
    private int stageUp; //升阶所需成就点
    private String award; //升阶获得的奖励
    private int reqperfect; //获得完美奖励需要的成就点
    private String perfectAward; //完美奖励
    private String perfectIcon; //完美奖励Icon
    private int roleLevel; //激活该阶段玩家所需等级
    private String icon; //段位的图标

    //内存内容
    private Map<Integer,Integer> stageUpAwardMap;
    private Map<Integer,Integer> perfectAwardMap;

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStageUp() {
        return stageUp;
    }

    public void setStageUp(int stageUp) {
        this.stageUp = stageUp;
    }

    public String getAward() {
        return award;
    }

    public void setAward(String award) {
        this.award = award;
        this.stageUpAwardMap = StringUtil.toMap(award,Integer.class,Integer.class,'+','|');
    }

    public int getReqperfect() {
        return reqperfect;
    }

    public void setReqperfect(int reqperfect) {
        this.reqperfect = reqperfect;
    }

    public String getPerfectAward() {
        return perfectAward;
    }

    public void setPerfectAward(String perfectAward) {
        this.perfectAward = perfectAward;
        this.perfectAwardMap = StringUtil.toMap(perfectAward,Integer.class,Integer.class,'+','|');
    }

    public String getPerfectIcon() {
        return perfectIcon;
    }

    public void setPerfectIcon(String perfectIcon) {
        this.perfectIcon = perfectIcon;
    }

    public int getRoleLevel() {
        return roleLevel;
    }

    public void setRoleLevel(int roleLevel) {
        this.roleLevel = roleLevel;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Map<Integer, Integer> getStageUpAwardMap() {
        return stageUpAwardMap;
    }

    public void setStageUpAwardMap(Map<Integer, Integer> stageUpAwardMap) {
        this.stageUpAwardMap = stageUpAwardMap;
    }

    public Map<Integer, Integer> getPerfectAwardMap() {
        return perfectAwardMap;
    }

    public void setPerfectAwardMap(Map<Integer, Integer> perfectAwardMap) {
        this.perfectAwardMap = perfectAwardMap;
    }
}
