package com.stars.modules.oldplayerback.pojo;

import com.stars.util.DateUtil;
import com.stars.util.StringUtil;

import java.util.Date;

/**
 * Created by huwenjun on 2017/7/13.
 */
public class AllRoleLimitConf {
    private int level;
    private int leavelHours;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLeavelHours() {
        return leavelHours;
    }

    public void setLeavelHours(int leavelHours) {
        this.leavelHours = leavelHours;
    }

    private AllRoleLimitConf(int level, int leavelHours) {
        this.level = level;
        this.leavelHours = leavelHours;
    }

    public static AllRoleLimitConf parse(String comeback_reward_limit1) {
        try {
            Integer[] group = StringUtil.toArray(comeback_reward_limit1, Integer[].class, ',');
            return new AllRoleLimitConf(group[0], group[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 全角色检测
     *
     * @param lastLoginTimestamp
     * @return
     */
    public boolean check(long lastLoginTimestamp) {
        return DateUtil.getHoursBetweenTwoDates(new Date(lastLoginTimestamp), new Date()) >= this.getLeavelHours();
    }
}
