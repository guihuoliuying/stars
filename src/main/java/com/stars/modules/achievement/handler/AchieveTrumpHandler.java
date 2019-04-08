package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.modules.trump.event.TrumpAchieveEvent;
import com.stars.modules.trump.userdata.RoleTrumpRow;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/8/8.
 */
public class AchieveTrumpHandler implements AchievementHandler {
    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        TrumpAchieveEvent event = (TrumpAchieveEvent) condition;
        Map<Integer, RoleTrumpRow> roleTrumpMap = event.getRoleTrumpMap();
        if(StringUtil.isEmpty(roleTrumpMap))
            return false;

        String[] limit = vo.getFunc().split("[+]");
        int achieveType = Integer.valueOf(limit[0]);
        int condition2 = Integer.valueOf(limit[1]);
        int condition3 = 0;
        if(limit.length >= 3){
            condition3 = Integer.valueOf(limit[2]);
        }

        boolean hasChange = false;
        if (achieveType == 1){ // 已收集的法宝数量
            if(roleTrumpMap.size() >= condition2){
                row.setState(AchievementRow.FINISH);
                hasChange = true;
            }
            int oldProcessing = 0;
            if (!StringUtil.isEmpty(row.getProcessing())) {
                oldProcessing = Integer.valueOf(row.getProcessing());
            }
            if(oldProcessing != roleTrumpMap.size()){
                row.setProcessing(String.valueOf(roleTrumpMap.size()));
                hasChange = true;
            }
        }else if (achieveType == 2){ //获得了指定法宝
            if (roleTrumpMap.containsKey(condition2)){
                row.setState(AchievementRow.FINISH);
                hasChange = true;
            }
        }else if (achieveType == 3){ //指定法宝达到多少级
            if (roleTrumpMap.containsKey(condition2)){
                RoleTrumpRow trumpRow = roleTrumpMap.get(condition2);
                if(trumpRow.getLevel() >= condition3){
                    row.setState(AchievementRow.FINISH);
                    hasChange = true;
                }
                int oldProcessing = 0;
                if (!StringUtil.isEmpty(row.getProcessing())) {
                    oldProcessing = Integer.valueOf(row.getProcessing());
                }
                if(oldProcessing != trumpRow.getLevel()){
                    row.setProcessing(String.valueOf(trumpRow.getLevel()));
                    hasChange = true;
                }
            }
        }
        return hasChange;
    }
}
