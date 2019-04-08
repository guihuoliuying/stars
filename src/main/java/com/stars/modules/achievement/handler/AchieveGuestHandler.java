package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.modules.guest.GuestManager;
import com.stars.modules.guest.event.GuestAchieveEvent;
import com.stars.modules.guest.prodata.GuestInfoVo;
import com.stars.modules.guest.userdata.RoleGuest;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by wuyuxing on 2017/2/9.
 */
public class AchieveGuestHandler implements AchievementHandler {
    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        GuestAchieveEvent event = (GuestAchieveEvent) condition;
        Map<Integer, RoleGuest> guestMap = event.getGuestMap();

        String[] limit = vo.getFunc().split("[+]");
        int achieveType = Integer.valueOf(limit[0]);
        int reqCount = Integer.valueOf(limit[1]);
        int param = 0;
        if(limit.length >=3){
            param = Integer.valueOf(limit[2]);
        }
        int count = 0;

        boolean hasChange = false;
        if(achieveType == 1){           //获得指定品质的门客数量
            GuestInfoVo guestInfoVo;
            for(RoleGuest roleGuest:guestMap.values()){
                if(roleGuest == null) continue;
                guestInfoVo = GuestManager.getInfoVo(roleGuest.getGuestId());
                if(guestInfoVo == null) continue;
                if(guestInfoVo.getQuality() == param){
                    count++;
                }
            }
        }else if(achieveType == 2){     //获得指定星级的门客数量
            for(RoleGuest roleGuest:guestMap.values()){
                if(roleGuest == null) continue;
                if(roleGuest.getLevel() >= param){
                    count++;
                }
            }
        }else if(achieveType == 3){     //指定门客达到指定星级
            RoleGuest roleGuest = guestMap.get(param);
            if(roleGuest != null){
                count = roleGuest.getLevel();
            }
        }else if(achieveType == 4){
            count = guestMap.size();
        }

        if(count >= reqCount){
            row.setState(AchievementRow.FINISH);
            hasChange = true;
        }else {
            int oldProcessing = 0;
            if (!StringUtil.isEmpty(row.getProcessing())) {
                oldProcessing = Integer.valueOf(row.getProcessing());
            }
            if(oldProcessing != count){
                row.setProcessing(String.valueOf(count));
                hasChange = true;
            }
        }
        return hasChange;
    }
}
