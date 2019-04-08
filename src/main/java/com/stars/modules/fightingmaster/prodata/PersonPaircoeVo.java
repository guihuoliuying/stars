package com.stars.modules.fightingmaster.prodata;

import com.stars.multiserver.fightingmaster.Matchable;
import com.stars.util.LogUtil;

/**
 * Created by zhouyaohui on 2016/11/17.
 */
public class PersonPaircoeVo implements Matchable, Cloneable{

    private String disscoresection; // 表现积分分段
    private String paircoe;         // 匹配积分计算参数

    private int minScore;
    private int maxScore;

    private int cminScore;
    private int cmaxScore;

    private int fightFactor;
    private int seqFactor;

    public String getDisscoresection() {
        return disscoresection;
    }

    public void setDisscoresection(String disscoresection) {
        this.disscoresection = disscoresection;
        String[] scores = disscoresection.split("[+]");
        minScore = Integer.valueOf(scores[0]);
        maxScore = Integer.valueOf(scores[1]);
        cminScore = minScore;
        cmaxScore = maxScore;
    }

    public String getPaircoe() {
        return paircoe;
    }

    public void setPaircoe(String paircoe) {
        this.paircoe = paircoe;
        String[] pair = paircoe.split("[+]");
        fightFactor = Integer.valueOf(pair[0]);
        seqFactor = Integer.valueOf(pair[1]);
    }

    public int getCminScore() {
        return cminScore;
    }

    public void setCminScore(int cminScore) {
        this.cminScore = cminScore;
    }

    public int getCmaxScore() {
        return cmaxScore;
    }

    public void setCmaxScore(int cmaxScore) {
        this.cmaxScore = cmaxScore;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public int getFightFactor() {
        return fightFactor;
    }

    public int getSeqFactor() {
        return seqFactor;
    }

    public PersonPaircoeVo copy() {
        try {
            return (PersonPaircoeVo) this.clone();
        } catch (CloneNotSupportedException e) {
            LogUtil.error("clone failed.", e);
        }
        return null;
    }

    @Override
    public int compare(Matchable other) {
        PersonPaircoeVo otherVo = (PersonPaircoeVo) other;
        if (cminScore > otherVo.getCmaxScore()) {
            return 1;
        }
        if (cmaxScore < otherVo.getCminScore()) {
            return -1;
        }
        return 0;
    }
}
