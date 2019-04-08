package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.modules.dungeon.event.ChapterStarAchieveEvent;

/**
 * Created by zhouyaohui on 2016/12/19.
 */
public class AchieveChaptHandler implements AchievementHandler {
    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        ChapterStarAchieveEvent event = (ChapterStarAchieveEvent) condition;
        String[] func = vo.getFunc().split("[+]");
        int chapterId = Integer.valueOf(func[0]);
        int star = Integer.valueOf(func[1]);
        if (event.getChapterId() == chapterId) {
            row.setProcessing(String.valueOf(event.getNewStr()));
        }
        if (event.getChapterId() == chapterId && event.getNewStr() >= star) {
            row.setState(AchievementRow.FINISH);
        }
        return true;
    }
}
