package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.modules.friend.event.FriendAchieveEvent;
import com.stars.util.StringUtil;

/**
 * Created by wuyuxing on 2017/2/11.
 */
public class AchieveFriendHandler implements AchievementHandler {
    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        FriendAchieveEvent event = (FriendAchieveEvent) condition;
        int friendNum = event.getFriendNum();
        int reqNum = Integer.valueOf(vo.getFunc());
        boolean hasChange = false;
        if(friendNum>=reqNum){
            row.setState(AchievementRow.FINISH);
            hasChange = true;
        }else{
            int oldProcessing = 0;
            if (!StringUtil.isEmpty(row.getProcessing())) {
                oldProcessing = Integer.valueOf(row.getProcessing());
            }
            if(oldProcessing != friendNum){
                row.setProcessing(String.valueOf(friendNum));
                hasChange = true;
            }
        }
        return hasChange;
    }
}
