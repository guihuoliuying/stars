package com.stars.modules.newfirstrecharge.pojo;

import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/9/6.
 */
public class NewFirstRechargeReward {
    private int vipLevelMin;
    private int vipLevelMax;
    private String rewardText;
    private Map<Integer, Integer> reward = new HashMap<>();

    public NewFirstRechargeReward(int vipLevelMin, int vipLevelMax, Map<Integer, Integer> reward) {
        this.vipLevelMin = vipLevelMin;
        this.vipLevelMax = vipLevelMax;
        this.reward = reward;
    }

    public int getVipLevelMin() {
        return vipLevelMin;
    }

    public void setVipLevelMin(int vipLevelMin) {
        this.vipLevelMin = vipLevelMin;
    }

    public int getVipLevelMax() {
        return vipLevelMax;
    }

    public void setVipLevelMax(int vipLevelMax) {
        this.vipLevelMax = vipLevelMax;
    }

    public Map<Integer, Integer> getReward() {
        return reward;
    }

    public void setReward(Map<Integer, Integer> reward) {
        this.reward = reward;
    }

    public boolean belong(int viplevel) {
        return viplevel >= vipLevelMin && viplevel <= vipLevelMax;
    }

    public static NewFirstRechargeReward parse(String data) {
        int index = data.indexOf("|");
        String headString = data.substring(0, index);
        String tailString = data.substring(index + 1);
        try {
            int[] vipLevels = StringUtil.toArray(headString, int[].class, '-');
            Map<Integer, Integer> reward = StringUtil.toMap(tailString, Integer.class, Integer.class, '+', '|');
            NewFirstRechargeReward newFirstRechargeReward = new NewFirstRechargeReward(vipLevels[0], vipLevels[1], reward);
            newFirstRechargeReward.setRewardText(tailString);
            return newFirstRechargeReward;
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public String getRewardText() {
        return rewardText;
    }

    public void setRewardText(String rewardText) {
        this.rewardText = rewardText;
    }
}
