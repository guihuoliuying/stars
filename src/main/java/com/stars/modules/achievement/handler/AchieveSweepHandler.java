package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.util.StringUtil;

/**
 * Created by zhouyaohui on 2016/10/20.
 */
public class AchieveSweepHandler implements AchievementHandler {
    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        int reqCount = Integer.valueOf(vo.getFunc());
        int curCount = 0;
        if (!StringUtil.isEmpty(row.getProcessing())) {
            curCount = Integer.valueOf(row.getProcessing());
        }
        curCount += (int) condition;
        row.setProcessing(String.valueOf(curCount));
        if (curCount >= reqCount) {
            row.setState(AchievementRow.FINISH);
        }
        return true;
    }
}
