package com.stars.modules.bestcp520.prodata;

/**
 * Created by huwenjun on 2017/5/20.
 */
public class BestCPRankReward {
    private int cpId;
    private String rankRange;//排名下限+排名上限
    private int reward;//dropid
    private int minRank;//排名下限
    private int maxRank;//排名上限

    public int getCpId() {
        return cpId;
    }

    public void setCpId(int cpId) {
        this.cpId = cpId;
    }

    public String getRankRange() {
        return rankRange;
    }

    public void setRankRange(String rankRange) {
        this.rankRange = rankRange;
        String[] rankRanges = rankRange.split("\\+");
        minRank = Integer.parseInt(rankRanges[0]);
        maxRank = Integer.parseInt(rankRanges[1]);
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public int getMinRank() {
        return minRank;
    }


    public int getMaxRank() {
        return maxRank;
    }

    public boolean checkBelongTo(int rank) {
        if (rank >= minRank && rank <= maxRank) {
            return true;
        }
        return false;
    }
}
