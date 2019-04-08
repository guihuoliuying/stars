package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.util.DateUtil;
import com.stars.util.StringUtil;

/**
 * Created by zhanghaizhen on 2017/8/8.
 */
public class AchieveSigninHandler implements AchievementHandler {
    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        String[] limit = vo.getFunc().split("[+]");
        int achieveType = Integer.valueOf(limit[0]);
        int reqCount = Integer.valueOf(limit[1]);

        boolean hasChange = false;
        if (achieveType == 1){
            int oldValue = 0;
            if (!StringUtil.isEmpty(row.getProcessing())) { //历史完成次数
                oldValue = Integer.valueOf(row.getProcessing());
            }
            int newValue = oldValue + 1;
            row.setProcessing(String.valueOf(newValue)); //新的完成次数
            if(newValue >= reqCount){
                row.setState(AchievementRow.FINISH);
            }
            hasChange = true;
        }else if (achieveType == 2){
            int ymd = 0;
            int curCount = 0;
            if (!StringUtil.isEmpty(row.getProcessing())) {
                String[] login = row.getProcessing().split("[+]");
                ymd = Integer.valueOf(login[0]);
                curCount = Integer.valueOf(login[1]);
            }
            int today = Integer.valueOf(DateUtil.getYMD_Str());
            if (today - ymd == 1) {
                curCount++;
            } else if (today - ymd != 0) {
                curCount = 1;
            }
            StringBuilder builder = new StringBuilder();
            builder.append(today).append("+").append(curCount);
            row.setProcessing(builder.toString());
            if (curCount >= reqCount) {
                row.setState(AchievementRow.FINISH);
            }
            hasChange = true;
        }

        return hasChange;
    }
}
