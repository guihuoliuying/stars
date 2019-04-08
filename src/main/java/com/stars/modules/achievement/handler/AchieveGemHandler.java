package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.modules.gem.prodata.GemLevelVo;

import java.util.List;

/**
 * Created by zhouyaohui on 2016/10/19.
 */
public class AchieveGemHandler implements AchievementHandler {

    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        List<GemLevelVo> list = (List<GemLevelVo>) condition;
        String[] func = vo.getFunc().split("[+]");
        int count = 0;
        for (GemLevelVo gem : list) {
            if (gem.getType() != Byte.valueOf(func[0])) {
                break;
            }
            if (gem.getLevel() >= Integer.valueOf(func[1])) {
                count++;
            }
        }
        row.setProcessing(String.valueOf(count));
        if (count >= Integer.valueOf(func[2])) {
            row.setState(AchievementRow.FINISH);
        }
        return true;
    }
}
