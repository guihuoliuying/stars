package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.modules.daily5v5.event.Daily5v5AchieveEvent;
import com.stars.multiserver.daily5v5.Daily5v5Manager;
import com.stars.util.StringUtil;

/**
 * Created by zhanghaizhen on 2017/8/8.
 */
public class AchieveDaily5v5Handlder implements AchievementHandler {
    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        Daily5v5AchieveEvent event = (Daily5v5AchieveEvent) condition;
        int result = event.getResult();
        boolean hasChange = false;
        String[] limit = vo.getFunc().split("[+]");
        int achieveType = Integer.valueOf(limit[0]);
        int condition2 = Integer.valueOf(limit[1]);
        //类型1表示参与次数 类型2表示胜利次数
        if (achieveType == 1 || (achieveType == 2 && result == Daily5v5Manager.WIN_RESULT)){
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
        }

        return hasChange;
    }
}
