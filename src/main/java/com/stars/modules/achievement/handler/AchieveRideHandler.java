package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.modules.ride.event.RideAchieveEvent;
import com.stars.util.StringUtil;

import java.util.Set;

/**
 * Created by zhouyaohui on 2016/10/19.
 */
public class AchieveRideHandler implements AchievementHandler {

    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        RideAchieveEvent event = (RideAchieveEvent) condition;
        Set<Integer> rideList = event.getRideList();
        int curLevelId = event.getCurLevelId();
        int ownCount = event.getOwnCount();

        String[] limit = vo.getFunc().split("[+]");
        int achieveType = Integer.valueOf(limit[0]);
        int reqValue = Integer.valueOf(limit[1]);
        boolean hasChange = false;

        if(achieveType == 1){           //达到对应坐骑等级
            if(curLevelId >= reqValue){
                row.setState(AchievementRow.FINISH);
                hasChange = true;
            }
        }else if(achieveType == 2){     //获得指定坐骑
            if(rideList.contains(reqValue)){
                row.setState(AchievementRow.FINISH);
                hasChange = true;
            }
        }else if(achieveType == 3){     //获得坐骑数量
            if(ownCount >= reqValue){
                row.setState(AchievementRow.FINISH);
                hasChange = true;
            }else{
                int oldProcessing = 0;
                if (!StringUtil.isEmpty(row.getProcessing())) {
                    oldProcessing = Integer.valueOf(row.getProcessing());
                }
                if(oldProcessing != ownCount){
                    row.setProcessing(String.valueOf(ownCount));
                    hasChange = true;
                }
            }
        }
        return hasChange;
    }
}
