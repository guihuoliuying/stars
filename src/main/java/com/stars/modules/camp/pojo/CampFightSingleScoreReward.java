package com.stars.modules.camp.pojo;

import java.util.Map;

/**
 * Created by huwenjun on 2017/7/25.
 */
public class CampFightSingleScoreReward {
    private int score;
    private Map<Integer,Integer> reward;

    public CampFightSingleScoreReward(int score, Map<Integer, Integer> reward) {
        this.score = score;
        this.reward = reward;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Map<Integer, Integer> getReward() {
        return reward;
    }

    public void setReward(Map<Integer, Integer> reward) {
        this.reward = reward;
    }
}
