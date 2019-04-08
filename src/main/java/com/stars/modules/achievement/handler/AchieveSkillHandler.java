package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhouyaohui on 2016/10/20.
 */
public class AchieveSkillHandler implements AchievementHandler {
    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        String[] skill = ((String) condition).split("[+]");
        String[] func = vo.getFunc().split("[+]");
        int reqLevel = Integer.valueOf(func[1]);
        int reqCount = Integer.valueOf(func[0]);


        Map<Integer, Integer> skillMap;
        if (StringUtil.isEmpty(row.getProcessing())) {
            skillMap = new HashMap<>();
        } else {
            skillMap = StringUtil.toMap(row.getProcessing(), Integer.class, Integer.class, '=', ',');
        }
        skillMap.put(Integer.valueOf(skill[0]), Integer.valueOf(skill[1]));
        row.setProcessing(StringUtil.makeString(skillMap, '=', ','));

        for (Integer level : skillMap.values()) {
            if (level >= reqLevel) {
                reqCount--;
            }
        }
        if (reqCount <= 0) {
            row.setState(AchievementRow.FINISH);
        }
        return true;
    }
}
