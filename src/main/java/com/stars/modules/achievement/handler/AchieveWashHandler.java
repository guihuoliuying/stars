package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.modules.newequipment.event.EquipExtAttrAchieveEvent;
import com.stars.modules.tool.userdata.ExtraAttrVo;

import java.util.Map;

/**
 * Created by zhouyaohui on 2016/12/20.
 */
public class AchieveWashHandler implements AchievementHandler {
    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        EquipExtAttrAchieveEvent event = (EquipExtAttrAchieveEvent) condition;
        String[] func = vo.getFunc().split("[+]");
        int achievePart = Integer.valueOf(func[0]);
        int achieveCount = Integer.valueOf(func[1]);
        int achieveQuality = Integer.valueOf(func[2]);
        int achieve = 0;
        for (Map<Byte, ExtraAttrVo> map : event.getExtAttrMap().values()) {
            int tempCount = achieveCount;
            for (ExtraAttrVo ext : map.values()) {
                if (ext.getQuality() >= achieveQuality) {
                    tempCount--;
                }
            }
            if (tempCount <= 0) {
                achievePart--;
                achieve++;
            }
        }
        row.setProcessing(String.valueOf(achieve));
        if (achievePart <= 0) {
            row.setState(AchievementRow.FINISH);
        }
        return true;
    }
}
