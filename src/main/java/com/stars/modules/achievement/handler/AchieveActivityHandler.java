package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.util.StringUtil;

/**
 * Created by zhouyaohui on 2016/10/19.
 */
public class AchieveActivityHandler implements AchievementHandler {

    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        boolean change = false;
        int activityId = (Integer) condition;
        String[] func = vo.getFunc().split("[+]");
        if (activityId == Integer.valueOf(func[0])) {
            int oldValue = 0;
            if (!StringUtil.isEmpty(row.getProcessing())) {
                oldValue = Integer.valueOf(row.getProcessing());
            }
            int newValue = oldValue + 1;
            row.setProcessing(String.valueOf(newValue));
            if (newValue >= Integer.valueOf(func[1])) {
                row.setState(AchievementRow.FINISH);
            }
            change = true;
        }
        return change;
    }
}
