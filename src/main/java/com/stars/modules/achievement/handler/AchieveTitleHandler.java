package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.modules.title.TitleManager;
import com.stars.modules.title.event.TitleAchieveEvent;
import com.stars.modules.title.prodata.TitleVo;
import com.stars.modules.title.userdata.RoleTitle;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/8/8.
 */
public class AchieveTitleHandler implements AchievementHandler {
    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        TitleAchieveEvent event = (TitleAchieveEvent) condition;
        Map<Integer, RoleTitle> roleTitleMap = event.getRoleTitleMap();
        if(StringUtil.isEmpty(roleTitleMap))
            return false;

        String[] limit = vo.getFunc().split("[+]");
        int achieveType = Integer.valueOf(limit[0]);
        int condition2 = Integer.valueOf(limit[1]);
        int condition3 = 0;
        if(limit.length >= 3){
            condition3 = Integer.valueOf(limit[2]);
        }

        boolean hasChange = false;
        if (achieveType == 1){  //称号个数超过一定数量
            int size = roleTitleMap.size();
            if(size >= condition2) {
                hasChange = true;
            }

        }else if (achieveType == 2){ //拥有某个id的称号
            if(roleTitleMap.containsKey(condition2)){
                hasChange = true;
            }

        }else if (achieveType == 3){
            int count = 0;
            for(int titleId: roleTitleMap.keySet()){
                TitleVo titleVo = TitleManager.getTitleVo(titleId);
                if(titleVo == null)
                    continue;
                if(titleVo.getType() != condition2)
                    continue;
                count++;
            }
            if(count >= condition3){
                hasChange = true;
            }
        }

        if(hasChange){
            row.setState(AchievementRow.FINISH);
        }

        return hasChange;
    }
}
