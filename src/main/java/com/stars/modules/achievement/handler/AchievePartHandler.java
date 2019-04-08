package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by zhouyaohui on 2016/12/20.
 */
public class AchievePartHandler implements AchievementHandler {
    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        String[] str = ((String) condition).split("[|]");
        int type = Integer.valueOf(str[0]);
        Map<Integer, Integer> map = StringUtil.toMap(str[1], Integer.class, Integer.class, '=', ',');
        String[] func = vo.getFunc().split("[+]");
        int achieveType = Integer.valueOf(func[1]);
        int achieveCount = Integer.valueOf(func[0]);
        int achieveValue = Integer.valueOf(func[2]);
        if (achieveType != type) {
            return false;
        }
        int achieve = 0;
        for (Integer value : map.values()) {
            if (value >= achieveValue) {
                achieveCount--;
                achieve++;
            }
        }
        row.setProcessing(String.valueOf(achieve));
        if (achieveCount <= 0) {
            row.setState(AchievementRow.FINISH);
        }
        return false;
    }
}
