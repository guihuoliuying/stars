package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.modules.elitedungeon.event.EliteDungonAchieveEvent;
import com.stars.util.StringUtil;

/**
 * Created by zhanghaizhen on 2017/8/8.
 */
public class AchieveEliteDungeonHandler  implements AchievementHandler {
    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        EliteDungonAchieveEvent event = (EliteDungonAchieveEvent) condition;
        int eliteDungonId = event.getEliteDungonId();
        String[] limit = vo.getFunc().split("[+]");
        int achieveType = Integer.valueOf(limit[0]);
        int condition2 = Integer.valueOf(limit[1]);
        int condition3 = condition2;  //只有两个参数的话，第三个参数默认和第二个一致
        if(limit.length >= 3){
            condition3 = Integer.valueOf(limit[2]);
        }
        boolean hasChange = false;
        // 类型1表示参与次数 类型2 表示某个精英副本参与次数
        if (achieveType == 1 || (achieveType == 2 && condition2 == eliteDungonId)){
            int oldValue = 0;
            if (!StringUtil.isEmpty(row.getProcessing())) { //历史完成次数
                oldValue = Integer.valueOf(row.getProcessing());
            }
            int newValue = oldValue + 1;
            row.setProcessing(String.valueOf(newValue)); //新的完成次数
            if(newValue >= condition3){
                row.setState(AchievementRow.FINISH);
            }
            hasChange = true;
        }

        return hasChange;
    }
}
