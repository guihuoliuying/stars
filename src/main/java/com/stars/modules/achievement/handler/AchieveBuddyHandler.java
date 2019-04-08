package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.modules.buddy.BuddyManager;
import com.stars.modules.buddy.event.BuddyAchieveEvent;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhouyaohui on 2016/10/19.
 */
public class AchieveBuddyHandler implements AchievementHandler {

    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        BuddyAchieveEvent event = (BuddyAchieveEvent) condition;
        String[] func = vo.getFunc().split("[+]");
        int buddyId = event.getBuddyId();
        int type = event.getType();
        int curLevel = event.getCurLevel();

        int achieveType1 = Integer.valueOf(func[0]);
        int achieveIdOrCount = Integer.valueOf(func[1]);
        int achieveQuality = Integer.valueOf(func[2]);
        int achieveType2 = Integer.valueOf(func[3]);
        int achieveValue = Integer.valueOf(func[4]);

        Map<Integer, Integer> buddyMap;
        int total = BuddyManager.buddyinfoVoMap.size();
        if (StringUtil.isEmpty(row.getProcessing())) {
            buddyMap = new HashMap<>();
            // 特殊处理，-1 表示产品数据总数，客户端需要用到
            buddyMap.put(-1, total);
        } else {
            buddyMap = StringUtil.toMap(row.getProcessing(), Integer.class, Integer.class, '=', ',');
        }
        buddyMap.put(buddyId, curLevel);
        row.setProcessing(StringUtil.makeString(buddyMap, '=', ','));
        buddyMap.remove(-1);    // 移除总数

        if (type != achieveType2) {
            return true;
        }

        if (achieveQuality != 0 && achieveQuality != event.getBuddyQuality()) {
            return true;
        }

        if (achieveType1 == 1) {
            for (Integer value : buddyMap.values()) {
                if (value >= achieveValue) {
                    achieveIdOrCount--;
                }
            }
            if (achieveIdOrCount <= 0) {
                row.setState(AchievementRow.FINISH);
            }
        }
        if (achieveType1 == 2) {
            for (Map.Entry<Integer, Integer> entry : buddyMap.entrySet()) {
                if (entry.getKey() == achieveIdOrCount && entry.getValue() >= achieveValue) {
                    row.setState(AchievementRow.FINISH);
                    break;
                }
            }
        }

        return true;
    }
}
