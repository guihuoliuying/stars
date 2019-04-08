package com.stars.modules.daily.prodata;

/**
 * 检查战力段数据
 * Created by zhanghaizhen on 2017/7/7.
 */
public class FightDeltaVo {
    private int minFightScoreDelta;
    private int maxFightScoreDelta;

    public FightDeltaVo(int minFightScoreDelta, int maxFightScoreDelta) {
        this.minFightScoreDelta = minFightScoreDelta;
        this.maxFightScoreDelta = maxFightScoreDelta;
    }

    public int getMinFightScoreDelta() {
        return minFightScoreDelta;
    }

    public void setMinFightScoreDelta(int minFightScoreDelta) {
        this.minFightScoreDelta = minFightScoreDelta;
    }

    public int getMaxFightScoreDelta() {
        return maxFightScoreDelta;
    }

    public void setMaxFightScoreDelta(int maxFightScoreDelta) {
        this.maxFightScoreDelta = maxFightScoreDelta;
    }
}
