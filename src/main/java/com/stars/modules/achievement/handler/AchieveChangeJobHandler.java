package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.modules.changejob.event.ChangeJobAchieveEvent;
import com.stars.util.StringUtil;

/**
 * Created by zhanghaizhen on 2017/8/8.
 */
public class AchieveChangeJobHandler implements AchievementHandler {
    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        ChangeJobAchieveEvent event = (ChangeJobAchieveEvent) condition;
        int newJobId = event.getJobId();
        boolean hasChange = false;
        String[] limit = vo.getFunc().split("[+]");
        int achieveType = Integer.valueOf(limit[0]);
        int condition2 = Integer.valueOf(limit[1]);
        if (achieveType == 1){  //累计转职次数
            int oldValue = 0;
            if (!StringUtil.isEmpty(row.getProcessing())) { //历史完成次数
                oldValue = Integer.valueOf(row.getProcessing());
            }
            int newValue = oldValue + 1;
            row.setProcessing(String.valueOf(newValue)); //新的完成次数
            if(newValue >= condition2){
                row.setState(AchievementRow.FINISH);
            }
            hasChange = true;
        }else if (achieveType == 2){ //转职成某个职业
            if (newJobId == condition2){
                row.setState(AchievementRow.FINISH);
                hasChange = true;
            }
        }

        return hasChange;
    }
}
