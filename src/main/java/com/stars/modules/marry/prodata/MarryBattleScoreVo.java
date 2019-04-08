package com.stars.modules.marry.prodata;

import com.stars.util.StringUtil;

/**
 * Created by zhanghaizhen on 2017/5/22.
 */
public class MarryBattleScoreVo {
    private int monsterId; //怪物表
    private String scorerange; //获得积分段
    //内存
    private int minScore; //最少获得分数
    private int maxSocre; //最多获得分数

    public int getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(int monsterId) {
        this.monsterId = monsterId;
    }

    public String getScorerange() {
        return scorerange;
    }

    public void setScorerange(String scorerange) throws Exception{
        this.scorerange = scorerange;
        if(StringUtil.isNotEmpty(scorerange)){
            int[] args = StringUtil.toArray(scorerange,int[].class,'+');
            minScore = args[0];
            maxSocre = args[1];
        }
    }

    public int getMinScore() {
        return minScore;
    }

    public void setMinScore(int minScore) {
        this.minScore = minScore;
    }

    public int getMaxSocre() {
        return maxSocre;
    }

    public void setMaxSocre(int maxSocre) {
        this.maxSocre = maxSocre;
    }
}
