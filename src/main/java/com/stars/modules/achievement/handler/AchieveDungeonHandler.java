package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.modules.scene.event.PassStageEvent;

/**
 * Created by zhouyaohui on 2016/12/19.
 */
public class AchieveDungeonHandler implements AchievementHandler {
    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        PassStageEvent event = (PassStageEvent) condition;
        String[] func = vo.getFunc().split("[+]");
        int dungeonId = Integer.valueOf(func[0]);
        int star = Integer.valueOf(func[1]);
        if (dungeonId == event.getStageId() && event.getStar() >= star) {
            row.setState(AchievementRow.FINISH);
            return true;
        }
        return false;
    }
}
