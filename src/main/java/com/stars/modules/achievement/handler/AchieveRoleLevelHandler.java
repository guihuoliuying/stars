package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;

/**
 * Created by zhouyaohui on 2016/10/19.
 */
public class AchieveRoleLevelHandler implements AchievementHandler {

    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        int roleLevel = (Integer) condition;
        int reqLevel = Integer.valueOf(vo.getFunc());
        if (roleLevel >= reqLevel) {
            row.setState(AchievementRow.FINISH);
            return true;
        }
        return false;
    }
}
