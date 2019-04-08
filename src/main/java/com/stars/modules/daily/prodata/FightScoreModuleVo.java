package com.stars.modules.daily.prodata;

import com.stars.util.StringUtil;

/**
 * Created by zhanghaizhen on 2017/7/5.
 */
public class FightScoreModuleVo {
    private String sysName;
    private int level;
    private String maxFightScore;
    private String fightScoreStage;

    //内存
    private int recommFightScore; //推荐战力

    public String getSysName() {
        return sysName;
    }

    public void setSysName(String sysName) {
        this.sysName = sysName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getMaxFightScore() {
        return maxFightScore;
    }

    public void setMaxFightScore(String maxFightScore) {
        this.maxFightScore = maxFightScore;
        if(StringUtil.isNotEmpty(maxFightScore)){
            String[] array  = maxFightScore.split("\\+");
            this.recommFightScore = Integer.parseInt(array[1]);
        }
    }

    public String getFightScoreStage() {
        return fightScoreStage;
    }

    public void setFightScoreStage(String fightScoreStage) {
        this.fightScoreStage = fightScoreStage;
    }

    public int getRecommFightScore() {
        return recommFightScore;
    }

    public void setRecommFightScore(int recommFightScore) {
        this.recommFightScore = recommFightScore;
    }
}
