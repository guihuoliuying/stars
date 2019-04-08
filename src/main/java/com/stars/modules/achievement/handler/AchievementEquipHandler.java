package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.modules.newequipment.event.EquipChangeAchieveEvent;

/**
 * Created by zhouyaohui on 2016/12/19.
 */
public class AchievementEquipHandler implements AchievementHandler {

    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        EquipChangeAchieveEvent event = (EquipChangeAchieveEvent) condition;
        String[] func = vo.getFunc().split("[+]");
        int count = Integer.valueOf(func[0]);
        int type = Integer.valueOf(func[1]);
        int value = Integer.valueOf(func[2]);
        int process = 0;
        if (type == 1) {
            for (int level : event.getEquipLevelMap().values()) {
                if (level >= value) {
                    process += 1;
                }
            }
        } else if (type == 2) {
            for (byte quality : event.getEquipQualityMap().values()) {
                if (quality >= value) {
                    process += 1;
                }
            }
        }
        row.setProcessing(String.valueOf(process));
        if (process >= count) {
            row.setState(AchievementRow.FINISH);
        }
        return true;
    }
}
