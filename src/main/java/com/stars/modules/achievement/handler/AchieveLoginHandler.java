package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.util.DateUtil;
import com.stars.util.StringUtil;

/**
 * Created by zhouyaohui on 2016/10/20.
 */
public class AchieveLoginHandler implements AchievementHandler {
    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        int ymd = 0;
        int curCount = 0;
        int reqCount = Integer.valueOf(vo.getFunc());
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
        return true;
    }
}
