package com.stars.multiserver.camp;

import com.stars.modules.camp.pojo.CampEquilibrium;
import com.stars.multiserver.camp.pojo.CampTypeScale;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * Created by huwenjun on 2017/7/24.
 */
public class CampUtils {
    public static final int TYPE_DAILY_REWARD = 1;//每日俸禄
    public static final int TYPE_ACTIVITY_REWARD = 2;//活动奖励


    /**
     * 当阵营人数不平衡时，处于人少的一方，任务获得额外奖励
     *
     * @param type
     * @param reward
     */
    public static void addExtReward(int type, int campType, Map<Integer, Integer> reward) {
        try {
            CampTypeScale campTypeScale;
            if (ServiceHelper.campLocalMainService() != null) {
                campTypeScale = ServiceHelper.campLocalMainService().getCampTypeScale();
            } else {
                campTypeScale = ServiceHelper.campRemoteMainService().getCurrentScale();
            }
            CampEquilibrium campEquilibrium = campTypeScale.getCampEquilibrium();
            if (campType == campTypeScale.getLowCampType() && campEquilibrium != null) {
                switch (type) {
                    case TYPE_DAILY_REWARD: {//每日俸禄
                        float officerRewardExt = campEquilibrium.getOfficerRewardExt();
                        for (Map.Entry<Integer, Integer> entry : reward.entrySet()) {
                            reward.put(entry.getKey(), (int) Math.floor(entry.getValue() * (1 + officerRewardExt)));
                        }
                    }
                    break;
                    case TYPE_ACTIVITY_REWARD: {//活动
                        float activityRewardExt = campEquilibrium.getActivityRewardExt();
                        for (Map.Entry<Integer, Integer> entry : reward.entrySet()) {
                            reward.put(entry.getKey(), (int) Math.floor(entry.getValue() * (1 + activityRewardExt)));
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }

    }
}
