package com.stars.modules.achievement.handler;

import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.prodata.AchievementVo;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.modules.book.BookManager;
import com.stars.modules.book.event.BookAchieveEvent;
import com.stars.modules.book.userdata.RoleBookUtil;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/8/8.
 */
public class AchieveBookHandler implements AchievementHandler {
    @Override
    public boolean handler(AchievementRow row, AchievementVo vo, Object condition) throws Exception {
        BookAchieveEvent event = (BookAchieveEvent) condition;
        Map<Integer, RoleBookUtil> roleBookUtilMap = event.getRoleBookUtilMap();
        if(StringUtil.isEmpty(roleBookUtilMap))
            return false;
        String[] limit = vo.getFunc().split("[+]");
        int achieveType = Integer.valueOf(limit[0]);
        int condition2 = Integer.valueOf(limit[1]);
        int condition3 = 0;
        if(limit.length >= 3){
            condition3 = Integer.valueOf(limit[2]);
        }
        boolean hasChange = false;
        if (achieveType == 1){ //激活的典籍数量
            int count = 0;
            for(RoleBookUtil book:roleBookUtilMap.values()){
                if(book.getBookStatus() == BookManager.BOOK_ACTIVE){
                    count ++;
                }
            }
            if(count >= condition2){
                row.setState(AchievementRow.FINISH);
                hasChange = true;
            }
            int oldProcessing = 0;
            if (!StringUtil.isEmpty(row.getProcessing())) {
                oldProcessing = Integer.valueOf(row.getProcessing());
            }
            if(oldProcessing != count){
                row.setProcessing(String.valueOf(count));
                hasChange = true;
            }
        }else if (achieveType == 2){  //激活某一个典籍
            if(roleBookUtilMap.containsKey(condition2)){
                RoleBookUtil book = roleBookUtilMap.get(condition2);
                if(book.getBookStatus() == BookManager.BOOK_ACTIVE){
                    row.setState(AchievementRow.FINISH);
                    hasChange = true;
                }
            }
        }else if(achieveType == 3){ //某一典籍的研读等级
            if(roleBookUtilMap.containsKey(condition2)){
                RoleBookUtil book = roleBookUtilMap.get(condition2);
                if(book.getBookLv() >= condition3){
                    row.setState(AchievementRow.FINISH);
                    hasChange = true;
                }
            }
        }

        return hasChange;
    }
}
