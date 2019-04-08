package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by zhouyaohui on 2016/10/18.
 */
public class AchieveToolHandler implements AchievementHandler {

    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        boolean change = false;
        String func = vo.getFunc();
        String[] item = func.split("[+]");
        Map<Integer, Integer> itemMap = (Map<Integer, Integer>) condition;
        for (Map.Entry<Integer, Integer> entry : itemMap.entrySet()) {
            if (entry.getKey().intValue() == Integer.valueOf(item[0]).intValue()) {
                int oldValue = 0;
                if (!StringUtil.isEmpty(row.getProcessing())) {
                    oldValue = Integer.valueOf(row.getProcessing());
                }
                int newValue = oldValue + entry.getValue();
                row.setProcessing(String.valueOf(newValue));
                if (newValue >= Integer.valueOf(item[1])) {
                    row.setState(AchievementRow.FINISH);
                }
                change = true;
            }
        }
        return change;
    }
}
