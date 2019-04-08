package com.stars.modules.camp.pojo;

import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by huwenjun on 2017/6/27.
 */
public class CampEquilibrium {
    private float min;
    private float max;
    private String minDesc;
    private String maxDesc;
    private Map<Integer, Integer> reward;
    private float officerRewardExt;
    private float activityRewardExt;

    public static CampEquilibrium parse(String param) {
        CampEquilibrium campEquilibrium = new CampEquilibrium();
        String[] sections = param.split(",");
        String scale = sections[0];
        try {
            Integer[] scaleArr = StringUtil.toArray(scale, Integer[].class, '+');
            campEquilibrium.setMin(scaleArr[0] / 1000.0f);
            campEquilibrium.setMax(scaleArr[1] / 1000.0f);
            String[] descArr = StringUtil.toArray(sections[1], String[].class, '+');
            campEquilibrium.setMinDesc(descArr[0]);
            campEquilibrium.setMaxDesc(descArr[1]);
            Map<Integer, Integer> reward = StringUtil.toMap(sections[2], Integer.class, Integer.class, '+', '|');
            campEquilibrium.setReward(reward);
            campEquilibrium.setOfficerRewardExt(Integer.parseInt(sections[3]) / 1000.0f);
            campEquilibrium.setActivityRewardExt(Integer.parseInt(sections[4])/1000.0f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return campEquilibrium;
    }

    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
    }

    public String getMinDesc() {
        return minDesc;
    }

    public void setMinDesc(String minDesc) {
        this.minDesc = minDesc;
    }

    public String getMaxDesc() {
        return maxDesc;
    }

    public void setMaxDesc(String maxDesc) {
        this.maxDesc = maxDesc;
    }

    public Map<Integer, Integer> getReward() {
        return reward;
    }

    public void setReward(Map<Integer, Integer> reward) {
        this.reward = reward;
    }

    public float getOfficerRewardExt() {
        return officerRewardExt;
    }

    public void setOfficerRewardExt(float officerRewardExt) {
        this.officerRewardExt = officerRewardExt;
    }

    public float getActivityRewardExt() {
        return activityRewardExt;
    }

    public void setActivityRewardExt(float activityRewardExt) {
        this.activityRewardExt = activityRewardExt;
    }

}
