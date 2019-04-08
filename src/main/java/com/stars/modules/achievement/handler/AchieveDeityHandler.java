package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.modules.deityweapon.event.ActiveDeityWeaponAchieveEvent;

/**
 * Created by wuyuxing on 2017/2/9.
 */
public class AchieveDeityHandler implements AchievementHandler {
    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        ActiveDeityWeaponAchieveEvent event = (ActiveDeityWeaponAchieveEvent) condition;
        byte type = event.getType();
        byte reqType = Byte.valueOf(vo.getFunc());
        if(reqType == type){
            row.setState(AchievementRow.FINISH);
            return true;
        }
        return false;
    }
}
